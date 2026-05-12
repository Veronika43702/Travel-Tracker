package ru.nikfirs.android.traveltracker.core.ui.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.nikfirs.android.traveltracker.core.domain.repository.DataStoreRepository
import ru.nikfirs.android.traveltracker.core.domain.repository.TripRepository
import ru.nikfirs.android.traveltracker.core.domain.repository.VisaRepository
import ru.nikfirs.android.traveltracker.core.ui.data.DataStoreRepositoryImpl
import ru.nikfirs.android.traveltracker.core.ui.data.TripRepositoryImpl
import ru.nikfirs.android.traveltracker.core.ui.data.VisaRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

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

    @Binds
    @Singleton
    abstract fun bindDataStoreRepository(
        dataStoreRepositoryImpl: DataStoreRepositoryImpl
    ): DataStoreRepository

}