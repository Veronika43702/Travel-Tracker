package ru.nikfirs.android.traveltracker.core.data.database.converter

import androidx.room.TypeConverter
import ru.nikfirs.android.traveltracker.core.data.database.entity.SegmentType
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripPurpose
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaCategory
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
    fun fromVisaCategory(category: VisaCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toVisaCategory(category: String?): VisaCategory? {
        return category?.let { VisaCategory.valueOf(it) }
    }

    @TypeConverter
    fun fromTripPurpose(tripPurpose: TripPurpose?): String? {
        return tripPurpose?.name
    }

    @TypeConverter
    fun toTripPurpose(tripPurpose: String?): TripPurpose? {
        return tripPurpose?.let { TripPurpose.valueOf(it) }
    }

    @TypeConverter
    fun fromSegmentType(segmentType: SegmentType?): String? {
        return segmentType?.name
    }

    @TypeConverter
    fun toSegmentType(segmentType: String?): SegmentType? {
        return segmentType?.let { SegmentType.valueOf(it) }
    }

    @TypeConverter
    fun fromStringList(value: String?): List<String>? {
        return value?.split(", ")
    }

    @TypeConverter
    fun fromListString(list: List<String>?): String? {
        return list?.joinToString(", ")
    }
}