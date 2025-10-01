package xyz.zephr.demo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.zephr.demo.data.repository.AuthRepositoryImpl
import xyz.zephr.demo.data.repository.PlacesRepositoryImpl
import xyz.zephr.demo.domain.repository.AuthRepository
import xyz.zephr.demo.domain.repository.PlacesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(
        placesRepositoryImpl: PlacesRepositoryImpl
    ): PlacesRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
