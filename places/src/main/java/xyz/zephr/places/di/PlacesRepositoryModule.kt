package xyz.zephr.places.di

import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import xyz.zephr.places.api.PlacesRepository
import xyz.zephr.places.auth.AuthRepository
import xyz.zephr.places.auth.AuthRepositoryImpl
import xyz.zephr.places.createPlacesRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlacesRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    companion object {

        @Provides
        @Singleton
        fun providePlacesRepository(
            @Named("auth") okHttpClient: OkHttpClient,
            gson: Gson
        ): PlacesRepository {
            return createPlacesRepository(
                okHttpClient = okHttpClient,
                gson = gson
            )
        }
    }
}
