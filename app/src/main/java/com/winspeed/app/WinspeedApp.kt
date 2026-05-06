package com.winspeed.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

@Composable
fun WinspeedApp(
    locationManager: LocationManager,
    orientationManager: OrientationManager,
    settingsDataStore: SettingsDataStore,
    onKioskModeChange: (Boolean) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    
    val savedTheme by settingsDataStore.themeFlow.collectAsState(initial = Theme.LIGHT)
    val savedLayout by settingsDataStore.layoutModeFlow.collectAsState(initial = LayoutMode.TWO_S)
    val savedWindMode by settingsDataStore.windModeFlow.collectAsState(initial = WindMode.MANUAL)
    val savedManualWindDirection by settingsDataStore.manualWindDirectionFlow.collectAsState(initial = 0f)

    var theme by remember { mutableStateOf(Theme.LIGHT) }
    var layout by remember { mutableStateOf(LayoutMode.TWO_S) }
    var windMode by remember { mutableStateOf(WindMode.MANUAL) }
    var manualWindDirection by remember { mutableStateOf(0f) }

    LaunchedEffect(savedTheme) { theme = savedTheme }
    LaunchedEffect(savedLayout) { layout = savedLayout }
    LaunchedEffect(savedWindMode) { windMode = savedWindMode }
    LaunchedEffect(savedManualWindDirection) { manualWindDirection = savedManualWindDirection }

    var recording by remember { mutableStateOf(false) }
    
    LaunchedEffect(recording) {
        onKioskModeChange(recording)
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
                SettingsScreen(
                    theme = theme,
                    layout = layout,
                    windDirection = manualWindDirection,
                    windMode = windMode,
                    textColor = textColor,
                    onThemeChange = { 
                        theme = it
                        coroutineScope.launch { settingsDataStore.saveTheme(it) }
                    },
                    onLayoutChange = { 
                        layout = it
                        coroutineScope.launch { settingsDataStore.saveLayoutMode(it) }
                    },
                    onWindChange = { 
                        manualWindDirection = it
                        coroutineScope.launch { settingsDataStore.saveManualWindDirection(it) }
                    },
                    onWindModeChange = { 
                        windMode = it
                        coroutineScope.launch { settingsDataStore.saveWindMode(it) }
                    },
                    onStart = { recording = true }
                )
            } else {
                val currentWind = when (windMode) {
                    WindMode.MANUAL -> manualWindDirection
                    WindMode.AUTO_TACK -> windEstimator.estimatedWindDirection
                }
                DashboardScreen(
                    layout = layout,
                    speed = speedKnots,
                    heading = headingDegrees,
                    vmg = vmg,
                    windDirection = currentWind,
                    bgColor = MaterialTheme.colorScheme.background,
                    textColor = textColor,
                    onExit = { recording = false }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    theme: Theme,
    layout: LayoutMode,
    windDirection: Float,
    windMode: WindMode,
    textColor: Color,
    onThemeChange: (Theme) -> Unit,
    onLayoutChange: (LayoutMode) -> Unit,
    onWindChange: (Float) -> Unit,
    onWindModeChange: (WindMode) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        WinspeedLogo(modifier = Modifier.padding(bottom = 24.dp))
        
        Text(
            text = "Winspeed",
            fontSize = 48.sp,
            color = textColor
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { onThemeChange(if (theme == Theme.LIGHT) Theme.DARK else Theme.LIGHT) },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Theme: ${theme.name}")
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
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Layout: ${layout.shortName}-data")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                onWindModeChange(
                    when (windMode) {
                        WindMode.MANUAL -> WindMode.AUTO_TACK
                        WindMode.AUTO_TACK -> WindMode.MANUAL
                    }
                )
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Wind Mode: ${windMode.name}")
        }

        if (windMode == WindMode.MANUAL) {
            Text("Manual Wind: ${windDirection.toInt()}°", color = textColor)
            Slider(
                value = windDirection,
                onValueChange = onWindChange,
                valueRange = 0f..359f,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        } else {
            Text("Auto (Tack) Mode Active", color = textColor)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.padding(8.dp)
        ) {
            Text("START", fontSize = 36.sp)
        }
    }
}

@Composable
fun DashboardScreen(
    layout: LayoutMode,
    speed: Float,
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
    val speedStr = "%.1f".format(speed)
    val headingStr = "${heading.toInt()}°"
    val vmgStr = "%.1f".format(vmg)
    val twaStr = "${SailingMath.calculateTWA(heading, windDirection).toInt()}°"

    val data = when (layout) {
        LayoutMode.TWO_S -> listOf("Speed" to speedStr, "Heading" to headingStr)
        LayoutMode.FOUR_Q, LayoutMode.FOUR_S -> listOf(
            "Speed" to speedStr, "VMG" to vmgStr,
            "Heading" to headingStr, "TWA" to twaStr
        )
        LayoutMode.SIX_Q, LayoutMode.SIX_S -> listOf(
            "Speed" to speedStr, "VMG" to vmgStr,
            "Heading" to headingStr, "TWA" to twaStr,
            "Tacking" to "0.0", "Polar" to "0%"
        )
    }

    val rows = when (layout) {
        LayoutMode.TWO_S -> 2
        LayoutMode.FOUR_Q -> 2
        LayoutMode.FOUR_S -> 4
        LayoutMode.SIX_Q -> 3
        LayoutMode.SIX_S -> 6
    }

    val cols = when (layout) {
        LayoutMode.TWO_S -> 1
        LayoutMode.FOUR_Q -> 2
        LayoutMode.FOUR_S -> 1
        LayoutMode.SIX_Q -> 2
        LayoutMode.SIX_S -> 1
    }

    val valueFontSize = when (layout) {
        LayoutMode.TWO_S -> 160.sp
        LayoutMode.FOUR_Q -> 85.sp
        LayoutMode.FOUR_S -> 140.sp
        LayoutMode.SIX_Q -> 80.sp
        LayoutMode.SIX_S -> 90.sp
    }

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
