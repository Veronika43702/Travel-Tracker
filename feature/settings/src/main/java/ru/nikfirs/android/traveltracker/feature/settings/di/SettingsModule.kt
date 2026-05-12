package ru.nikfirs.android.traveltracker.feature.settings.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.nikfirs.android.traveltracker.feature.settings.data.RepositoryImpl
import ru.nikfirs.android.traveltracker.feature.settings.domain.Repository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindRepository(
        repositoryImpl: RepositoryImpl
    ): Repository
}