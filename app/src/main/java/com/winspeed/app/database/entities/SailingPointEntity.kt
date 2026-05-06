package com.winspeed.app.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sailing_points",
    foreignKeys = [
        ForeignKey(
            entity = SailingSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SailingPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val speedKnots: Float,
    val headingDegrees: Float,
    val gpsBearing: Float? = null,
    val magneticHeading: Float? = null,
    val altitude: Double = 0.0,
    
    // --- Positional Accuracy & Environment ---
    val accuracy: Float = 0f,
    val pressure: Float? = null,
    val temperature: Float? = null,
    val humidity: Float? = null,
    
    // --- Attitude & Motion ---
    val orientation: FloatArray? = null,
    val acceleration: FloatArray? = null,
    val gyroscope: FloatArray? = null,
    val magneticField: FloatArray? = null,
    val linearAcceleration: FloatArray? = null,
    val gravity: FloatArray? = null,
    val rotationVector: FloatArray? = null,
    
    // --- Device Health & Status ---
    val batteryLevel: Int? = null,
    val batteryTemperature: Float? = null,
    val isCharging: Boolean? = null,
    
    // --- Signal & Technical Metadata ---
    val satellitesInView: Int? = null,
    val gpsTime: Long? = null,
    val nmeaSentences: List<String>? = null,
    
    // --- Advanced Technical / Uncalibrated Data ---
    val uncalibratedGyroscope: FloatArray? = null,
    val uncalibratedMagneticField: FloatArray? = null,
    val ambientLight: Float? = null,
    val stepCount: Float? = null,
    
    // --- Specialized Fusion & Environment ---
    val gameRotationVector: FloatArray? = null,
    val geomagneticRotationVector: FloatArray? = null,
    val accelerometerUncalibrated: FloatArray? = null,
    val proximity: Float? = null,
    val internalTemperature: Float? = null,
    val audioAmplitude: Float? = null,
    val wifiSignalStrength: Int? = null,
    val cellSignalStrength: Int? = null,
    val heartRate: Float? = null,
    
    // --- High-End Pro Layer ---
    val uvIndex: Float? = null,
    val pose6Dof: FloatArray? = null,
    val gnssRawMeasurements: String? = null,
    val cpuUsage: Float? = null,
    val memoryUsage: Long? = null,
    val diskSpaceAvailable: Long? = null,
    val bluetoothDevicesCount: Int? = null,
    val networkType: String? = null,
    
    // --- Hardware States & Triggers ---
    val hingeAngle: Float? = null,
    val nativeHeading: Float? = null,
    val heartBeat: Boolean? = null,
    val offBodyDetect: Boolean? = null,
    val tiltDetector: Boolean? = null,
    val significantMotion: Boolean? = null,
    val limitedAxesAccelerometer: FloatArray? = null,
    val limitedAxesGyroscope: FloatArray? = null,
    val thermalStatus: Int? = null,
    val rawSensors: Map<String, Any>? = null
)
