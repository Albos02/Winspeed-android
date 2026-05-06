package com.winspeed.app.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromFloatArray(value: FloatArray?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toFloatArray(value: String?): FloatArray? {
        return value?.let { gson.fromJson(it, FloatArray::class.java) }
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return value?.let { gson.fromJson(it, type) }
    }

    @TypeConverter
    fun fromRawSensors(value: Map<String, Any>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toRawSensors(value: String?): Map<String, Any>? {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return value?.let { gson.fromJson(it, type) }
    }
}
