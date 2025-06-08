package ru.nikfirs.android.traveltracker.core.data.database.converter

import androidx.room.TypeConverter
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripPurpose
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it, dateFormatter) }
    }

    @TypeConverter
    fun fromVisaType(visaType: VisaType?): String? {
        return visaType?.name
    }

    @TypeConverter
    fun toVisaType(visaType: String?): VisaType? {
        return visaType?.let { VisaType.valueOf(it) }
    }

    @TypeConverter
    fun fromTripPurpose(tripPurpose: TripPurpose?): String? {
        return tripPurpose?.name
    }

    @TypeConverter
    fun toTripPurpose(tripPurpose: String?): TripPurpose? {
        return tripPurpose?.let { TripPurpose.valueOf(it) }
    }
}