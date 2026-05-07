package com.winspeed.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.winspeed.app.database.WinspeedDatabase
import com.winspeed.app.database.SailingDao
import com.winspeed.app.database.entities.SailingPointEntity
import com.winspeed.app.database.entities.SailingSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordingService : Service() {

    companion object {
        const val CHANNEL_ID = "WinspeedRecordingChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_RESUME_SESSION = "ACTION_RESUME_SESSION"
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_WIND_DIRECTION = "EXTRA_WIND_DIRECTION"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var locationManager: LocationManager? = null
    private var orientationManager: OrientationManager? = null
    private var collectionJob: Job? = null
    private var isPaused = false
    
    private var database: WinspeedDatabase? = null
    private var sailingDao: SailingDao? = null
    private var currentSessionId: Long? = null
    private val pointBuffer = mutableListOf<SailingPointEntity>()
    private val BUFFER_SIZE = 10

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        locationManager = LocationManager(this)
        orientationManager = OrientationManager(this)
        database = WinspeedDatabase.getDatabase(this)
        sailingDao = database?.sailingDao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> {
                val lastWind = if (intent.hasExtra(EXTRA_WIND_DIRECTION)) {
                    intent.getFloatExtra(EXTRA_WIND_DIRECTION, 0f)
                } else null
                stopRecording(lastWind)
            }
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_RESUME_SESSION -> {
                val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
                if (sessionId > 0) resumeSession(sessionId)
            }
        }
        return START_STICKY
    }

    private fun startRecording() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Winspeed")
            .setContentText("Recording session in progress...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        
        serviceScope.launch(Dispatchers.IO) {
            val session = SailingSession(startTime = System.currentTimeMillis())
            val realId = sailingDao?.insertSession(session) ?: return@launch
            
            withContext(Dispatchers.Main) {
                currentSessionId = realId
                locationManager?.startLocationUpdates()
                orientationManager?.start()
                collectData()
            }
        }
    }

    private fun collectData() {
        val locManager = locationManager ?: return
        val oriManager = orientationManager ?: return
        val dao = sailingDao ?: return
        val sessionId = currentSessionId ?: return
        
        collectionJob?.cancel()
        collectionJob = serviceScope.launch(Dispatchers.IO) {
            combine(locManager.locationData, oriManager.heading) { location, heading ->
                location to heading
            }.collect { (location, heading) ->
                if (!isPaused && location != null) {
                    val speedKnots = if (location.hasSpeed()) {
                        (location.speed * 1.94384f) // m/s to knots
                    } else 0f
                    
                    val sailingPoint = SailingPoint(
                        timestamp = System.currentTimeMillis(),
                        latitude = location.latitude,
                        longitude = location.longitude,
                        speedKnots = speedKnots,
                        headingDegrees = heading,
                        gpsBearing = if (location.hasBearing()) location.bearing else null,
                        altitude = if (location.hasAltitude()) location.altitude else 0.0,
                        accuracy = if (location.hasAccuracy()) location.accuracy else 0f
                    )
                    
                    val entity = SailingPointEntity(
                        sessionId = sessionId,
                        timestamp = sailingPoint.timestamp,
                        latitude = sailingPoint.latitude,
                        longitude = sailingPoint.longitude,
                        speedKnots = sailingPoint.speedKnots,
                        headingDegrees = sailingPoint.headingDegrees,
                        gpsBearing = sailingPoint.gpsBearing,
                        altitude = sailingPoint.altitude,
                        accuracy = sailingPoint.accuracy
                    )
                    
                    pointBuffer.add(entity)
                    if (pointBuffer.size >= BUFFER_SIZE) {
                        val toInsert = pointBuffer.toList()
                        pointBuffer.clear()
                        dao.insertPoints(toInsert)
                    }
                }
            }
        }
    }

    private fun pauseRecording() {
        isPaused = true
    }
    
    private fun resumeRecording() {
        isPaused = false
    }
    
    private fun resumeSession(sessionId: Long) {
        currentSessionId = sessionId
        isPaused = false
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Winspeed")
            .setContentText("Recording session in progress...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        
        locationManager?.startLocationUpdates()
        orientationManager?.start()
        
        collectData()
    }
    
    private fun stopRecording(lastWind: Float? = null) {
        isPaused = false
        collectionJob?.cancel()
        collectionJob = null
        
        val sessionId = currentSessionId
        val dao = sailingDao
        
        serviceScope.launch(Dispatchers.IO) {
            if (sessionId != null && dao != null) {
                // Flush buffer
                if (pointBuffer.isNotEmpty()) {
                    dao.insertPoints(pointBuffer.toList())
                    pointBuffer.clear()
                }

                val count = dao.getPointCount(sessionId)
                val max = dao.getMaxSpeed(sessionId) ?: 0f
                val avg = dao.getAvgSpeed(sessionId) ?: 0f
                
                dao.updateSession(
                    dao.getSessionById(sessionId)?.copy(
                        endTime = System.currentTimeMillis(),
                        pointCount = count,
                        maxSpeedKnots = max,
                        avgSpeedKnots = avg,
                        lastWindDirection = lastWind
                    ) ?: return@launch
                )
            }
            
            withContext(Dispatchers.Main) {
                locationManager?.stopLocationUpdates()
                orientationManager?.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        locationManager?.stopLocationUpdates()
        orientationManager?.stop()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Winspeed Recording Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
