package ru.nikfirs.android.traveltracker.core.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import ru.nikfirs.android.traveltracker.core.data.database.converter.Converters
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripDao
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripSegmentDao
import ru.nikfirs.android.traveltracker.core.data.database.dao.VisaDao
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.TripSegmentEntity
import ru.nikfirs.android.traveltracker.core.data.database.entity.VisaEntity

@Database(
    entities = [
        VisaEntity::class,
        TripEntity::class,
        TripSegmentEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TravelDatabase : RoomDatabase() {

    abstract fun visaDao(): VisaDao
    abstract fun tripDao(): TripDao
    abstract fun tripSegmentDao(): TripSegmentDao

    companion object {
        private const val DATABASE_NAME = "travel_tracker_database"

        @Volatile
        private var INSTANCE: TravelDatabase? = null

        fun getInstance(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}