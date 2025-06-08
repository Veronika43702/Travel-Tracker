package ru.nikfirs.android.traveltracker.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.nikfirs.android.traveltracker.core.data.database.TravelDatabase
import ru.nikfirs.android.traveltracker.core.data.database.dao.TripDao
import ru.nikfirs.android.traveltracker.core.data.database.dao.VisaDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTravelDatabase(
        @ApplicationContext context: Context
    ): TravelDatabase {
        return TravelDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideVisaDao(
        database: TravelDatabase
    ): VisaDao {
        return database.visaDao()
    }

    @Provides
    @Singleton
    fun provideTripDao(
        database: TravelDatabase
    ): TripDao {
        return database.tripDao()
    }
}