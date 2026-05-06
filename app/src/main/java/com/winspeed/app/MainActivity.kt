package com.winspeed.app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.winspeed.app.ui.theme.WinspeedTheme

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    private lateinit var locationManager: LocationManager
    private lateinit var orientationManager: OrientationManager
    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        locationManager = LocationManager(this)
        orientationManager = OrientationManager(this)
        settingsDataStore = SettingsDataStore(this)

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

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            WinspeedApp(
                locationManager = locationManager,
                orientationManager = orientationManager,
                settingsDataStore = settingsDataStore,
                onKioskModeChange = { enabled ->
                    if (enabled) enableKioskMode() else disableKioskMode()
                }
            )
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