package com.winspeed.app

import com.winspeed.app.database.entities.SailingPointEntity
import com.winspeed.app.database.entities.SailingSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object SessionExporter {
    private val gpxDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private fun formatTime(timestamp: Long): String = gpxDateFormat.format(Date(timestamp))
    
    fun toGpx(session: SailingSession, points: List<SailingPointEntity>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<gpx version=\"1.1\" creator=\"Winspeed\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
        sb.append("  <metadata>\n")
        sb.append("    <name>Sailing Session</name>\n")
        sb.append("    <time>")
        sb.append(formatTime(session.startTime))
        sb.append("</time>\n")
        sb.append("  </metadata>\n")
        sb.append("  <trk>\n")
        sb.append("    <name>Session ")
        sb.append(session.id)
        sb.append("</name>\n")
        sb.append("    <trkseg>\n")
        
        for (point in points) {
            sb.append("      <trkpt lat=\"")
            sb.append(point.latitude)
            sb.append("\" lon=\"")
            sb.append(point.longitude)
            sb.append("\">\n")
            if (point.altitude != 0.0) {
                sb.append("        <ele>")
                sb.append(point.altitude)
                sb.append("</ele>\n")
            }
            sb.append("        <time>")
            sb.append(formatTime(point.timestamp))
            sb.append("</time>\n")
            sb.append("      </trkpt>\n")
        }
        
        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>")
        
        return sb.toString()
    }
    
    fun toJson(session: SailingSession, points: List<SailingPointEntity>): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"session\": {\n")
        sb.append("    \"id\": ")
        sb.append(session.id)
        sb.append(",\n")
        sb.append("    \"startTime\": ")
        sb.append(session.startTime)
        sb.append(",\n")
        sb.append("    \"endTime\": ")
        if (session.endTime != null) {
            sb.append(session.endTime)
        } else {
            sb.append("null")
        }
        sb.append(",\n")
        sb.append("    \"name\": \"")
        sb.append(session.name ?: "Session ${session.id}")
        sb.append("\"\n")
        sb.append("  },\n")
        sb.append("  \"points\": [\n")
        
        points.forEachIndexed { index, point ->
            sb.append("    {\n")
            sb.append("      \"timestamp\": ")
            sb.append(point.timestamp)
            sb.append(",\n")
            sb.append("      \"latitude\": ")
            sb.append(point.latitude)
            sb.append(",\n")
            sb.append("      \"longitude\": ")
            sb.append(point.longitude)
            sb.append(",\n")
            sb.append("      \"speedKnots\": ")
            sb.append(point.speedKnots)
            sb.append(",\n")
            sb.append("      \"headingDegrees\": ")
            sb.append(point.headingDegrees)
            sb.append(",\n")
            sb.append("      \"gpsBearing\": ")
            if (point.gpsBearing != null) {
                sb.append(point.gpsBearing)
            } else {
                sb.append("null")
            }
            sb.append(",\n")
            sb.append("      \"altitude\": ")
            sb.append(point.altitude)
            sb.append(",\n")
            sb.append("      \"accuracy\": ")
            sb.append(point.accuracy)
            sb.append("\n")
            sb.append("    }")
            if (index < points.size - 1) sb.append(",")
            sb.append("\n")
        }
        
        sb.append("  ],\n")
        sb.append("  \"summary\": {\n")
        sb.append("    \"totalPoints\": ")
        sb.append(points.size)
        sb.append(",\n")
        val avgSpeed = if (points.isNotEmpty()) points.map { it.speedKnots }.average() else 0.0
        sb.append("    \"avgSpeedKnots\": ")
        sb.append(String.format(Locale.US, "%.1f", avgSpeed))
        sb.append(",\n")
        val maxSpeed = if (points.isNotEmpty()) points.maxOf { it.speedKnots } else 0f
        sb.append("    \"maxSpeedKnots\": ")
        sb.append(String.format(Locale.US, "%.1f", maxSpeed))
        sb.append("\n")
        sb.append("  }\n")
        sb.append("}\n")
        
        return sb.toString()
    }
}