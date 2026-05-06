package com.winspeed.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Theme { LIGHT, DARK }
enum class LayoutMode { TWO_S, FOUR_Q, FOUR_S, SIX_Q, SIX_S }

@Composable
fun WinspeedApp(locationManager: LocationManager) {
    var theme by remember { mutableStateOf(Theme.LIGHT) }
    var layout by remember { mutableStateOf(LayoutMode.TWO_S) }
    var recording by remember { mutableStateOf(false) }

    val location by locationManager.locationData.collectAsState()
    
    var speedKnots by remember { mutableStateOf(0f) }
    var headingDegrees by remember { mutableStateOf(0f) }

    LaunchedEffect(location) {
        val rawSpeed = (location?.speed ?: 0f) * 1.94384f
        val rawHeading = location?.bearing ?: 0f
        
        if (rawSpeed > 0.2f) {
            speedKnots = rawSpeed
            headingDegrees = rawHeading
        } else {
            // If speed is low, wait 10 seconds before zeroing out
            delay(10000)
            // Re-check after delay (LaunchedEffect will be cancelled if a new location comes and rawSpeed > 0.2)
            speedKnots = 0f
            headingDegrees = 0f
        }
    }

    val isDark = theme == Theme.DARK
    val bgColor = if (isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
    val textColor = if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.Black

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bgColor
    ) {
        if (!recording) {
            SettingsScreen(
                theme = theme,
                layout = layout,
                textColor = textColor,
                onThemeChange = { theme = it },
                onLayoutChange = { layout = it },
                onStart = { recording = true }
            )
        } else {
            DashboardScreen(
                layout = layout,
                speed = speedKnots,
                heading = headingDegrees,
                bgColor = bgColor,
                textColor = textColor,
                onExit = { recording = false }
            )
        }
    }
}

@Composable
fun SettingsScreen(
    theme: Theme,
    layout: LayoutMode,
    textColor: androidx.compose.ui.graphics.Color,
    onThemeChange: (Theme) -> Unit,
    onLayoutChange: (LayoutMode) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Settings",
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
            Text("Layout: ${layout.name}-data")
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
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    onExit: () -> Unit
) {
    val speedStr = "%.1f".format(speed)
    val headingStr = "${heading.toInt()}°"

    val data = when (layout) {
        LayoutMode.TWO_S -> listOf("Speed" to speedStr, "Heading" to headingStr)
        LayoutMode.FOUR_Q, LayoutMode.FOUR_S -> listOf(
            "Speed" to speedStr, "VMG" to "0.0",
            "Heading" to headingStr, "Wind" to "0°"
        )
        LayoutMode.SIX_Q, LayoutMode.SIX_S -> listOf(
            "Speed" to speedStr, "VMG" to "0.0",
            "Heading" to headingStr, "Wind" to "0°",
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Button(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Text("EXIT")
        }
    }
}

@Composable
fun DataCell(
    label: String,
    value: String,
    textColor: androidx.compose.ui.graphics.Color,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 80.sp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = textColor.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(2.dp, textColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 28.sp,
                color = textColor
            )
            Text(
                text = value,
                fontSize = valueFontSize,
                color = textColor
            )
        }
    }
}