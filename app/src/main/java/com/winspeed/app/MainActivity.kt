package com.winspeed.app

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.winspeed.app.database.entities.SailingSession
import com.winspeed.app.database.WinspeedDatabase
import com.winspeed.app.ui.theme.WinspeedTheme

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.winspeed.app.database.SessionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var orientationManager: OrientationManager
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var sessionsRepository: SessionsRepository
    private var pendingResumeSessionId: Long? = null
    
    private val _sessions = MutableStateFlow<List<SailingSession>>(emptyList())
    private val sessions: StateFlow<List<SailingSession>> = _sessions.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        locationManager = LocationManager(this)
        orientationManager = OrientationManager(this)
        settingsDataStore = SettingsDataStore(this)
        
        val db = WinspeedDatabase.getDatabase(this)
        sessionsRepository = SessionsRepository(db.sailingDao())

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    locationManager.startLocationUpdates()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    locationManager.startLocationUpdates()
                }
            }
        }

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        locationPermissionRequest.launch(permissions.toTypedArray())

        // Check for crash recovery - resume incomplete session
        lifecycleScope.launch {
            val wasRecording = settingsDataStore.recordingFlow.first()
            if (wasRecording) {
                val incompleteSession = sessionsRepository.getIncompleteSession()
                if (incompleteSession != null) {
                    pendingResumeSessionId = incompleteSession.id
                }
            }
            // Cleanup any other sessions that were left open from previous crashes
            sessionsRepository.cleanupAbandonedSessions(pendingResumeSessionId)
        }

        // Load sessions for list
        lifecycleScope.launch {
            sessionsRepository.getAllSessions().collect { sessionList ->
                _sessions.value = sessionList
            }
        }

        lifecycleScope.launch {
            if (settingsDataStore.recordingFlow.first()) {
                setKeepScreenOn(true)
            }
        }

        setContent {
            val sessionList by sessions.collectAsState()
            WinspeedApp(
                locationManager = locationManager,
                orientationManager = orientationManager,
                settingsDataStore = settingsDataStore,
                sessions = sessionList,
                onKioskModeChange = { enabled ->
                    if (enabled) enableKioskMode() else disableKioskMode()
                },
                onRecordingStart = {
                    setKeepScreenOn(true)
                    val intent = Intent(this, RecordingService::class.java).apply {
                        action = RecordingService.ACTION_START
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                },
                onRecordingStop = { lastWind ->
                    setKeepScreenOn(false)
                    val intent = Intent(this, RecordingService::class.java).apply {
                        action = RecordingService.ACTION_STOP
                        if (lastWind != null) {
                            putExtra(RecordingService.EXTRA_WIND_DIRECTION, lastWind)
                        }
                    }
                    startService(intent)
                },
                onRecordingPause = {
                    val intent = Intent(this, RecordingService::class.java).apply {
                        action = RecordingService.ACTION_PAUSE
                    }
                    startService(intent)
                },
                onRecordingResume = {
                    val intent = Intent(this, RecordingService::class.java).apply {
                        action = RecordingService.ACTION_RESUME
                    }
                    startService(intent)
                },
                onResumeSession = {
                    setKeepScreenOn(true)
                    pendingResumeSessionId?.let { id ->
                        val intent = Intent(this, RecordingService::class.java).apply {
                            action = RecordingService.ACTION_RESUME_SESSION
                            putExtra(RecordingService.EXTRA_SESSION_ID, id)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(intent)
                        } else {
                            startService(intent)
                        }
                        pendingResumeSessionId = null
                    }
                },
                onExport = { sessionId, format, downloadOnly ->
                    exportSession(sessionId, format, downloadOnly)
                }
            )
        }
    }

    private fun setKeepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    private fun exportSession(sessionId: Long, format: String, downloadOnly: Boolean = false) {
        lifecycleScope.launch {
            try {
                val session = sessionsRepository.getSessionById(sessionId)
                if (session == null) {
                    Toast.makeText(this@MainActivity, "Session not found", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val points = mutableListOf<com.winspeed.app.database.entities.SailingPointEntity>()
                sessionsRepository.getPointsForSession(sessionId).first().let { points.addAll(it) }
                
                val content = if (format == "gpx") {
                    SessionExporter.toGpx(session, points)
                } else {
                    SessionExporter.toJson(session, points)
                }
                
                val filename = "winspeed_session_${sessionId}.$format"
                val mimeType = if (format == "gpx") "application/gpx+xml" else "application/json"
                
                if (downloadOnly) {
                    saveFileToDownloads(filename, content, mimeType)
                    return@launch
                }
                
                // Save to cache for sharing
                val file = java.io.File(cacheDir, filename)
                file.writeText(content)
                
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    this@MainActivity,
                    "${packageName}.fileprovider",
                    file
                )
                
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                startActivity(Intent.createChooser(shareIntent, "Export Session"))
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveFileToDownloads(filename: String, content: String, mimeType: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    Toast.makeText(this, "Saved to Downloads folder", Toast.LENGTH_LONG).show()
                } else {
                    throw Exception("Could not create MediaStore entry")
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = java.io.File(downloadsDir, filename)
                file.writeText(content)
                Toast.makeText(this, "Saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun enableKioskMode() {
        // Sticky Immersive Mode
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        
        // Pin the app (requires user confirmation if not device owner)
        try {
            startLockTask()
        } catch (e: Exception) {
            // Log or handle
        }
    }

    private fun disableKioskMode() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        
        try {
            stopLockTask()
        } catch (e: Exception) {
            // Log or handle
        }
    }

    override fun onResume() {
        super.onResume()
        orientationManager.start()
    }

    override fun onPause() {
        super.onPause()
        orientationManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationUpdates()
    }
}