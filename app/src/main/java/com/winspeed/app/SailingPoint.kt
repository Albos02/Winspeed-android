package com.winspeed.app

import java.util.Date

data class SailingPoint(
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double,
    val longitude: Double,
    val speedKnots: Float,
    val headingDegrees: Float,
    val altitude: Double = 0.0,
    
    // --- Positional Accuracy & Environment ---
    val accuracy: Float = 0f, // GPS horizontal accuracy in meters
    val pressure: Float? = null, // Barometric pressure for weather/altitude
    val temperature: Float? = null, // Ambient air temperature
    val humidity: Float? = null, // Relative humidity
    
    // --- Attitude & Motion (Heel, Pitch, Surge) ---
    val orientation: FloatArray? = null, // [azimuth, pitch, roll] - boat's attitude
    val acceleration: FloatArray? = null, // [x, y, z] - includes gravity
    val gyroscope: FloatArray? = null, // [x, y, z] - rate of rotation
    val magneticField: FloatArray? = null, // [x, y, z] - raw magnetic compass data
    val linearAcceleration: FloatArray? = null, // [x, y, z] - motion without gravity (jolts/waves)
    val gravity: FloatArray? = null, // [x, y, z] - isolated gravity vector for heel calculation
    val rotationVector: FloatArray? = null, // [x, y, z, scalar] - most stable fused orientation
    
    // --- Device Health & Status ---
    val batteryLevel: Int? = null, // Battery percentage
    val batteryTemperature: Float? = null, // Internal battery temp to monitor overheating
    val isCharging: Boolean? = null, // If device is on external power
    
    // --- Signal & Technical Metadata ---
    val satellitesInView: Int? = null, // GPS signal quality indicator
    val gpsTime: Long? = null, // Precise time from atomic clocks
    val nmeaSentences: List<String>? = null, // Raw NMEA 0183 log for pro analysis
    
    // --- Advanced Technical / Uncalibrated Data ---
    val uncalibratedGyroscope: FloatArray? = null, // Raw rotation + bias estimation
    val uncalibratedMagneticField: FloatArray? = null, // Raw magnetism + hard iron bias
    val ambientLight: Float? = null, // Day/Night detection
    val stepCount: Float? = null, // Crew activity tracking
    
    // --- Specialized Fusion & Environment ---
    val gameRotationVector: FloatArray? = null, // Orientation ignoring magnetic interference
    val geomagneticRotationVector: FloatArray? = null, // Low-power stable orientation
    val accelerometerUncalibrated: FloatArray? = null, // Raw acceleration + bias
    val proximity: Float? = null, // Detects if phone is in a pocket/case
    val internalTemperature: Float? = null, // Core CPU temperature
    val audioAmplitude: Float? = null, // Sound level in dB (wind/engine noise)
    val wifiSignalStrength: Int? = null, // Connectivity quality (dBm)
    val cellSignalStrength: Int? = null, // Mobile signal quality (dBm)
    val heartRate: Float? = null, // Biometric data if synced
    
    // --- High-End Pro Layer ---
    val uvIndex: Float? = null, // Sun exposure monitoring
    val pose6Dof: FloatArray? = null, // High-precision 6-degree-of-freedom tracking
    val gnssRawMeasurements: String? = null, // Carrier phase data for CM-level accuracy (PPK/RTK)
    val cpuUsage: Float? = null, // System load monitoring
    val memoryUsage: Long? = null, // RAM usage tracking
    val diskSpaceAvailable: Long? = null, // Storage limit monitoring
    val bluetoothDevicesCount: Int? = null, // Proximity of other crew/beacons
    val networkType: String? = null, // 4G/5G/Wifi status
    
    // --- Hardware States & Triggers ---
    val hingeAngle: Float? = null, // For foldable device support
    val nativeHeading: Float? = null, // Android 13+ platform-native heading
    val heartBeat: Boolean? = null, // Individual pulse event
    val offBodyDetect: Boolean? = null, // Detects if device was removed from person
    val tiltDetector: Boolean? = null, // Detects significant tilt changes
    val significantMotion: Boolean? = null, // Detects movement starts/stops
    val limitedAxesAccelerometer: FloatArray? = null, // For specialized hardware
    val limitedAxesGyroscope: FloatArray? = null, // For specialized hardware
    val thermalStatus: Int? = null, // OS-level thermal throttling state
    val rawSensors: Map<String, Any>? = null // Catch-all for proprietary/future sensors
)
