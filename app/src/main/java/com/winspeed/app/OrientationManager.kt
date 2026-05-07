package com.winspeed.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OrientationManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    // Low-pass filter coefficient (0.0 to 1.0, lower is smoother but slower)
    private val alpha = 0.1f

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = if (gravity == null) {
                event.values.clone()
            } else {
                lowPass(event.values, gravity!!)
            }
        }
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = if (geomagnetic == null) {
                event.values.clone()
            } else {
                lowPass(event.values, geomagnetic!!)
            }
        }

        if (gravity != null && geomagnetic != null) {
            val r = FloatArray(9)
            val i = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                
                // Convert azimuth from radians to degrees [0, 360]
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                if (azimuth < 0) azimuth += 360f
                
                _heading.value = azimuth
            }
        }
    }

    private fun lowPass(input: FloatArray, output: FloatArray): FloatArray {
        for (idx in input.indices) {
            output[idx] = output[idx] + alpha * (input[idx] - output[idx])
        }
        return output
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
