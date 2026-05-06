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
            WinspeedTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WinspeedApp(locationManager, orientationManager, settingsDataStore)
                }
            }
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