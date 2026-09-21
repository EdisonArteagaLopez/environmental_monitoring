package com.example.environmental_monitoring.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromAlarmType(type: AlarmType): String = type.name

    @TypeConverter
    fun toAlarmType(value: String): AlarmType = AlarmType.valueOf(value)
}
