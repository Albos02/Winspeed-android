package com.winspeed.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.winspeed.app.database.entities.SailingSession
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun SessionsListScreen(
    sessions: List<SailingSession>,
    textColor: Color,
    onBack: () -> Unit,
    onExport: (Long, String, Boolean) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    var exportTarget by remember { mutableStateOf<Pair<Long, String>?>(null) }
    
    if (exportTarget != null) {
        AlertDialog(
            onDismissRequest = { exportTarget = null },
            title = { Text("Export ${exportTarget?.second?.uppercase()}", fontSize = 18.sp) },
            text = { Text("Choose export method:", fontSize = 14.sp) },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { exportTarget = null }) {
                        Text("Cancel", fontSize = 14.sp)
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = {
                            val target = exportTarget ?: return@TextButton
                            onExport(target.first, target.second, false) // Share
                            exportTarget = null
                        }) {
                            Text("Share", fontSize = 14.sp)
                        }
                        Button(onClick = {
                            val target = exportTarget ?: return@Button
                            onExport(target.first, target.second, true) // Download
                            exportTarget = null
                        }) {
                            Text("Download", fontSize = 14.sp)
                        }
                    }
                }
            },
            dismissButton = null
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Past Sessions",
                fontSize = 24.sp,
                color = textColor
            )
            Button(onClick = onBack) {
                Text("Back", fontSize = 14.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (sessions.isEmpty()) {
            Text(
                text = "No sessions recorded yet",
                fontSize = 16.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sessions.size) { index ->
                    val session = sessions[index]
                    SessionCardItem(
                        session = session,
                        textColor = textColor,
                        onExportClick = { format -> exportTarget = session.id to format }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun SessionCardItem(
    session: SailingSession,
    textColor: Color,
    onExportClick: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val dateStr = dateFormat.format(session.startTime)
    
    val duration = session.endTime?.let { end ->
        val millis = end - session.startTime
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val hours = minutes / 60
        val mins = minutes % 60
        if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
    } ?: "In progress"

    val maxSpeedStr = String.format(Locale.US, "%.1f kts", session.maxSpeedKnots)
    val avgSpeedStr = String.format(Locale.US, "%.1f kts", session.avgSpeedKnots)
    val windStr = session.lastWindDirection?.let { "${it.toInt()}°" } ?: "--"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = textColor.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Date
            Text(
                text = dateStr,
                fontSize = 20.sp,
                color = textColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = textColor.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            
            StatLabel("Duration", duration, textColor)
            Spacer(modifier = Modifier.height(8.dp))
            StatLabel("GPS points", session.pointCount.toString(), textColor)
            Spacer(modifier = Modifier.height(8.dp))
            StatLabel("Wind direction", windStr, textColor)
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = textColor.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))
            
            StatLabel("Max speed", maxSpeedStr, textColor)
            Spacer(modifier = Modifier.height(8.dp))
            StatLabel("Avg speed", avgSpeedStr, textColor)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Export buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onExportClick("gpx") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("GPX", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = { onExportClick("json") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("JSON", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StatLabel(label: String, value: String, textColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = textColor
        )
    }
}