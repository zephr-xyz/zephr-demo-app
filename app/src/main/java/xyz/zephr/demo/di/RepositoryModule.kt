package xyz.zephr.demo.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.zephr.demo.data.repository.AuthRepositoryImpl
import xyz.zephr.demo.domain.repository.AuthRepository
import xyz.zephr.places.api.PlacesRepository
import xyz.zephr.places.network.PlacesApiService
import xyz.zephr.places.repository.defaultPlacesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    companion object {

        @Provides
        @Singleton
        fun providePlacesRepository(
            placesApiService: PlacesApiService
        ): PlacesRepository {
            return defaultPlacesRepository(placesApiService)
        }
    }
}
