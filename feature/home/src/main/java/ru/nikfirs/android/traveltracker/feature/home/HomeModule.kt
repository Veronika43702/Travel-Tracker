package ru.nikfirs.android.traveltracker.feature.home

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import ru.nikfirs.android.traveltracker.feature.home.data.TripRepositoryImpl
import ru.nikfirs.android.traveltracker.feature.home.data.VisaRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVisaRepository(
        visaRepositoryImpl: VisaRepositoryImpl
    ): VisaRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository
}