package com.winspeed.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration

import com.winspeed.app.database.entities.SailingSession
import com.winspeed.app.ui.theme.WinspeedTheme

enum class Theme { LIGHT, DARK }
enum class LayoutMode { 
    TWO_S, FOUR_Q, FOUR_S, SIX_Q, SIX_S;
    
    val shortName: String
        get() = when (this) {
            TWO_S -> "2s"
            FOUR_Q -> "4q"
            FOUR_S -> "4s"
            SIX_Q -> "6q"
            SIX_S -> "6s"
        }
}
enum class WindMode { MANUAL, AUTO_TACK }
enum class SpeedUnit { 
    KNOTS, KMH, MS;
    
    val displayName: String
        get() = when (this) {
            KNOTS -> "kn"
            KMH -> "km/h"
            MS -> "m/s"
        }
}
enum class AppOrientation { AUTO, PORTRAIT, LANDSCAPE }
enum class AppScreen { SETTINGS, DASHBOARD, SESSIONS }

@Composable
fun WinspeedApp(
    locationManager: LocationManager,
    orientationManager: OrientationManager,
    settingsDataStore: SettingsDataStore,
    sessions: List<com.winspeed.app.database.entities.SailingSession> = emptyList(),
    onKioskModeChange: (Boolean) -> Unit = {},
    onWakeLockChange: (Boolean) -> Unit = {},
    onRecordingStart: () -> Unit = {},
    onRecordingStop: (Float?) -> Unit = {},
    onRecordingPause: () -> Unit = {},
    onRecordingResume: () -> Unit = {},
    onResumeSession: () -> Unit = {},
    onOrientationChange: (AppOrientation) -> Unit = {},
    onExport: (Long, String, Boolean) -> Unit = { _, _, _ -> }
) {
    val coroutineScope = rememberCoroutineScope()
    
    val savedTheme by settingsDataStore.themeFlow.collectAsState(initial = Theme.LIGHT)
    val savedLayout by settingsDataStore.layoutModeFlow.collectAsState(initial = LayoutMode.TWO_S)
    val savedWindMode by settingsDataStore.windModeFlow.collectAsState(initial = WindMode.MANUAL)
    val savedSpeedUnit by settingsDataStore.speedUnitFlow.collectAsState(initial = SpeedUnit.KNOTS)
    val savedManualWindDirection by settingsDataStore.manualWindDirectionFlow.collectAsState(initial = 0f)
    val savedDashboardTextScale by settingsDataStore.dashboardTextScaleFlow.collectAsState(initial = 1.0f)
    val savedOrientation by settingsDataStore.orientationFlow.collectAsState(initial = AppOrientation.AUTO)
    val savedRecording by settingsDataStore.recordingFlow.collectAsState(initial = false)

    var theme by remember { mutableStateOf(Theme.LIGHT) }
    var layout by remember { mutableStateOf(LayoutMode.TWO_S) }
    var windMode by remember { mutableStateOf(WindMode.MANUAL) }
    var speedUnit by remember { mutableStateOf(SpeedUnit.KNOTS) }
    var manualWindDirection by remember { mutableStateOf(0f) }
    var dashboardTextScale by remember { mutableFloatStateOf(1.0f) }
    var orientation by remember { mutableStateOf(AppOrientation.AUTO) }
    var recording by remember { mutableStateOf(false) }
    var appScreen by remember { mutableStateOf(AppScreen.SETTINGS) }

    LaunchedEffect(savedTheme) { theme = savedTheme }
    LaunchedEffect(savedLayout) { layout = savedLayout }
    LaunchedEffect(savedWindMode) { windMode = savedWindMode }
    LaunchedEffect(savedSpeedUnit) { speedUnit = savedSpeedUnit }
    LaunchedEffect(savedManualWindDirection) { manualWindDirection = savedManualWindDirection }
    LaunchedEffect(savedDashboardTextScale) { dashboardTextScale = savedDashboardTextScale }
    LaunchedEffect(savedOrientation) { orientation = savedOrientation }
    LaunchedEffect(savedRecording) { recording = savedRecording }

    LaunchedEffect(recording, orientation) {
        onKioskModeChange(recording)
        onWakeLockChange(recording)
        val targetOrientation = if (recording && orientation == AppOrientation.AUTO) {
            AppOrientation.LANDSCAPE
        } else {
            orientation
        }
        onOrientationChange(targetOrientation)
    }

    val windEstimator = remember { WindEstimator() }

    val location by locationManager.locationData.collectAsState()
    val magneticHeadingRaw by orientationManager.heading.collectAsState()
    
    var speedKnots by remember { mutableStateOf(0f) }
    var headingDegrees by remember { mutableStateOf(0f) }
    var gpsBearingRaw by remember { mutableStateOf(0f) }
    
    var vmg by remember { mutableStateOf(0f) }
    var twa by remember { mutableStateOf(0f) }

    LaunchedEffect(location, magneticHeadingRaw) {
        val rawSpeed = (location?.speed ?: 0f) * 1.94384f
        val gpsBearing = location?.bearing ?: headingDegrees

        speedKnots = if (rawSpeed > 0.2f) rawSpeed else 0f
        gpsBearingRaw = gpsBearing
        
        headingDegrees = SailingMath.fuseHeading(
            gpsBearing = gpsBearing,
            magneticHeading = magneticHeadingRaw,
            speedKnots = rawSpeed
        )
        
        windEstimator.addHeading(headingDegrees, speedKnots)
        
        val currentWind = when (windMode) {
            WindMode.MANUAL -> manualWindDirection
            WindMode.AUTO_TACK -> windEstimator.estimatedWindDirection
        }
        
        vmg = SailingMath.calculateVMG(speedKnots, headingDegrees, currentWind)
        twa = SailingMath.calculateTWA(headingDegrees, currentWind)
    }

    WinspeedTheme(darkTheme = theme == Theme.DARK) {
        val textColor = if (theme == Theme.DARK) Color.White else Color.Black
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!recording) {
                when (appScreen) {
                    AppScreen.SETTINGS -> SettingsScreen(
                        theme = theme,
                        layout = layout,
                        speedUnit = speedUnit,
                        dashboardTextScale = dashboardTextScale,
                        orientation = orientation,
                        windDirection = manualWindDirection,
                        windMode = windMode,
                        sessions = sessions,
                        textColor = textColor,
                        onThemeChange = { 
                            theme = it
                            coroutineScope.launch { settingsDataStore.saveTheme(it) }
                        },
                        onLayoutChange = { 
                            layout = it
                            coroutineScope.launch { settingsDataStore.saveLayoutMode(it) }
                        },
                        onSpeedUnitChange = {
                            speedUnit = it
                            coroutineScope.launch { settingsDataStore.saveSpeedUnit(it) }
                        },
                        onDashboardTextScaleChange = {
                            dashboardTextScale = it
                            coroutineScope.launch { settingsDataStore.saveDashboardTextScale(it) }
                        },
                        onOrientationChange = {
                            orientation = it
                            coroutineScope.launch { settingsDataStore.saveOrientation(it) }
                        },
                        onWindChange = { 
                            manualWindDirection = it
                            coroutineScope.launch { settingsDataStore.saveManualWindDirection(it) }
                        },
                        onWindModeChange = { 
                            windMode = it
                            coroutineScope.launch { settingsDataStore.saveWindMode(it) }
                        },
                        onStart = { 
                            recording = true 
                            coroutineScope.launch { settingsDataStore.saveRecording(true) }
                            onRecordingStart()
                        },
                        onViewSessions = { appScreen = AppScreen.SESSIONS }
                    )
                    AppScreen.SESSIONS -> SessionsListScreen(
                        sessions = sessions,
                        textColor = textColor,
                        onBack = { appScreen = AppScreen.SETTINGS },
                        onExport = onExport
                    )
                    else -> {}
                }
            } else {
                val currentWind = when (windMode) {
                    WindMode.MANUAL -> manualWindDirection
                    WindMode.AUTO_TACK -> windEstimator.estimatedWindDirection
                }
                DashboardScreen(
                    layout = layout,
                    speed = speedKnots,
                    speedUnit = speedUnit,
                    textScale = dashboardTextScale,
                    heading = headingDegrees,
                    vmg = vmg,
                    windDirection = currentWind,
                    bgColor = MaterialTheme.colorScheme.background,
                    textColor = textColor,
                    onExit = { 
                        recording = false 
                        coroutineScope.launch { settingsDataStore.saveRecording(false) }
                        onRecordingStop(currentWind)
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    theme: Theme,
    layout: LayoutMode,
    speedUnit: SpeedUnit,
    dashboardTextScale: Float,
    orientation: AppOrientation,
    windDirection: Float,
    windMode: WindMode,
    sessions: List<com.winspeed.app.database.entities.SailingSession>,
    textColor: Color,
    onThemeChange: (Theme) -> Unit,
    onLayoutChange: (LayoutMode) -> Unit,
    onSpeedUnitChange: (SpeedUnit) -> Unit,
    onDashboardTextScaleChange: (Float) -> Unit,
    onOrientationChange: (AppOrientation) -> Unit,
    onWindChange: (Float) -> Unit,
    onWindModeChange: (WindMode) -> Unit,
    onStart: () -> Unit,
    onViewSessions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WinspeedLogo(modifier = Modifier.width(225.dp).height(185.dp).offset(y = (-70).dp))
        
        Text(
            text = "Winspeed",
            fontSize = 44.sp,
            color = textColor,
            modifier = Modifier.offset(y = (-70).dp)
        )

        OutlinedButton(
            onClick = { onThemeChange(if (theme == Theme.LIGHT) Theme.DARK else Theme.LIGHT) },
            modifier = Modifier.offset(y = (-40).dp).padding(4.dp).fillMaxWidth(0.7f)
        ) {
            Text("Theme: ${theme.name}")
        }

        OutlinedButton(
            onClick = {
                onSpeedUnitChange(
                    when (speedUnit) {
                        SpeedUnit.KNOTS -> SpeedUnit.KMH
                        SpeedUnit.KMH -> SpeedUnit.MS
                        SpeedUnit.MS -> SpeedUnit.KNOTS
                    }
                )
            },
            modifier = Modifier.offset(y = (-40).dp).padding(4.dp).fillMaxWidth(0.7f)
        ) {
            Text("Unit: ${speedUnit.displayName}")
        }

        OutlinedButton(
            onClick = {
                onLayoutChange(
                    when (layout) {
                        LayoutMode.TWO_S -> LayoutMode.FOUR_Q
                        LayoutMode.FOUR_Q -> LayoutMode.FOUR_S
                        LayoutMode.FOUR_S -> LayoutMode.SIX_Q
                        LayoutMode.SIX_Q -> LayoutMode.SIX_S
                        LayoutMode.SIX_S -> LayoutMode.TWO_S
                    }
                )
            },
            modifier = Modifier.offset(y = (-40).dp).padding(4.dp).fillMaxWidth(0.7f)
        ) {
            Text("Layout: ${layout.shortName}-data")
        }

        OutlinedButton(
            onClick = {
                onWindModeChange(
                    when (windMode) {
                        WindMode.MANUAL -> WindMode.AUTO_TACK
                        WindMode.AUTO_TACK -> WindMode.MANUAL
                    }
                )
            },
            modifier = Modifier.offset(y = (-40).dp).padding(4.dp).fillMaxWidth(0.7f)
        ) {
            Text("Wind: ${windMode.name}")
        }

        OutlinedButton(
            onClick = {
                onOrientationChange(
                    when (orientation) {
                        AppOrientation.AUTO -> AppOrientation.PORTRAIT
                        AppOrientation.PORTRAIT -> AppOrientation.LANDSCAPE
                        AppOrientation.LANDSCAPE -> AppOrientation.AUTO
                    }
                )
            },
            modifier = Modifier.offset(y = (-40).dp).padding(4.dp).fillMaxWidth(0.7f)
        ) {
            Text("Orientation: ${orientation.name}")
        }

        if (windMode == WindMode.MANUAL) {
            Text("Manual Wind: ${windDirection.toInt()}°", color = textColor, fontSize = 14.sp, modifier = Modifier.offset(y = (-35).dp))
            Slider(
                value = windDirection,
                onValueChange = onWindChange,
                valueRange = 0f..359f,
                modifier = Modifier.padding(horizontal = 32.dp).height(32.dp).offset(y = (-35).dp)
            )
        } else {
            Text("Auto (Tack) Mode Active", color = textColor, fontSize = 14.sp, modifier = Modifier.offset(y = (-35).dp))
            Spacer(modifier = Modifier.height(8.dp).offset(y = (-35).dp))
        }

        Text("Data Text Scale: ${"%.1f".format(dashboardTextScale)}x", color = textColor, fontSize = 14.sp, modifier = Modifier.offset(y = (-30).dp))
        Slider(
            value = dashboardTextScale,
            onValueChange = onDashboardTextScaleChange,
            valueRange = 0.5f..2.0f,
            modifier = Modifier.padding(horizontal = 32.dp).height(32.dp).offset(y = (-30).dp)
        )

        Spacer(modifier = Modifier.height(8.dp).offset(y = (-25).dp))

        Button(
            onClick = onStart,
            modifier = Modifier.offset(y = (-25).dp).padding(8.dp)
        ) {
            Text("START", fontSize = 36.sp)
        }
        
        val sessionCount = sessions.size
        if (sessionCount > 0) {
            OutlinedButton(
                onClick = onViewSessions,
                modifier = Modifier.offset(y = (-25).dp).padding(8.dp)
            ) {
                Text("Sessions ($sessionCount)", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DashboardScreen(
    layout: LayoutMode,
    speed: Float,
    speedUnit: SpeedUnit,
    textScale: Float,
    heading: Float,
    vmg: Float,
    windDirection: Float,
    bgColor: Color,
    textColor: Color,
    onExit: () -> Unit
) {
    BackHandler {
        // Prevent back button from exiting recording screen
    }

    var lastExitClickTime by remember { mutableLongStateOf(0L) }
    
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val convertedSpeed = when (speedUnit) {
        SpeedUnit.KNOTS -> speed
        SpeedUnit.KMH -> speed * 1.852f
        SpeedUnit.MS -> speed * 0.514444f
    }
    
    val convertedVmg = when (speedUnit) {
        SpeedUnit.KNOTS -> vmg
        SpeedUnit.KMH -> vmg * 1.852f
        SpeedUnit.MS -> vmg * 0.514444f
    }

    val speedStr = "%.1f".format(convertedSpeed)
    val headingStr = "${heading.toInt()}°"
    val vmgStr = "%.1f".format(convertedVmg)
    val twaStr = "${SailingMath.calculateTWA(heading, windDirection).toInt()}°"

    val speedLabel = "Speed (${speedUnit.displayName})"
    val vmgLabel = "VMG (${speedUnit.displayName})"

    val data = when (layout) {
        LayoutMode.TWO_S -> listOf(speedLabel to speedStr, "Heading" to headingStr)
        LayoutMode.FOUR_Q, LayoutMode.FOUR_S -> listOf(
            speedLabel to speedStr, vmgLabel to vmgStr,
            "Heading" to headingStr, "TWA" to twaStr
        )
        LayoutMode.SIX_Q, LayoutMode.SIX_S -> listOf(
            speedLabel to speedStr, vmgLabel to vmgStr,
            "Heading" to headingStr, "TWA" to twaStr,
            "Tacking" to "0.0", "Polar" to "0%"
        )
    }

    val rows = when (layout) {
        LayoutMode.TWO_S -> if (isLandscape) 1 else 2
        LayoutMode.FOUR_Q -> 2
        LayoutMode.FOUR_S -> if (isLandscape) 2 else 4
        LayoutMode.SIX_Q -> if (isLandscape) 2 else 3
        LayoutMode.SIX_S -> if (isLandscape) 2 else 6
    }

    val cols = when (layout) {
        LayoutMode.TWO_S -> if (isLandscape) 2 else 1
        LayoutMode.FOUR_Q -> 2
        LayoutMode.FOUR_S -> if (isLandscape) 2 else 1
        LayoutMode.SIX_Q -> if (isLandscape) 3 else 2
        LayoutMode.SIX_S -> if (isLandscape) 3 else 1
    }

    val baseFontSize = when (layout) {
        LayoutMode.TWO_S -> if (isLandscape) 140f else 160f
        LayoutMode.FOUR_Q -> 85f
        LayoutMode.FOUR_S -> if (isLandscape) 85f else 140f
        LayoutMode.SIX_Q -> if (isLandscape) 70f else 80f
        LayoutMode.SIX_S -> if (isLandscape) 70f else 90f
    }
    val valueFontSize = (baseFontSize * textScale).sp

    Box(modifier = Modifier.fillMaxSize().padding(2.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            for (i in 0 until rows) {
                Row(modifier = Modifier.weight(1f)) {
                    for (j in 0 until cols) {
                        val index = i * cols + j
                        if (index < data.size) {
                            DataCell(
                                label = data[index].first,
                                value = data[index].second,
                                textColor = textColor,
                                valueFontSize = valueFontSize,
                                modifier = Modifier.weight(1f).padding(2.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f).padding(2.dp))
                        }
                    }
                }
            }
        }

        // Wind Direction in top-left corner
        Text(
            text = "W: ${windDirection.toInt()}°",
            fontSize = 12.sp,
            color = textColor,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )

        // Touch blocking overlay except for the EXIT button area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Consume all touch events
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Do nothing
                }
        )

        Button(
            onClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastExitClickTime < 200) {
                    onExit()
                } else {
                    lastExitClickTime = currentTime
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(width = 80.dp, height = 40.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (System.currentTimeMillis() - lastExitClickTime < 200) 
                    Color.Red 
                else 
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                if (System.currentTimeMillis() - lastExitClickTime < 200) "CONFIRM" else "EXIT",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun DataCell(
    label: String,
    value: String,
    textColor: Color,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 80.sp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = textColor.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, textColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 16.sp,
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = valueFontSize,
                color = textColor
            )
        }
    }
}
