package xyz.zephr.places.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import xyz.zephr.places.BuildConfig
import xyz.zephr.places.auth.AuthEventBus
import xyz.zephr.places.auth.AuthInterceptor
import xyz.zephr.places.auth.AuthTokenStore
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlacesNetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .create()
    }

    @Provides
    @Singleton
    fun provideAuthEventBus(): AuthEventBus = AuthEventBus()

    @Provides
    @Singleton
    fun provideAuthTokenStore(@ApplicationContext context: Context): AuthTokenStore {
        return AuthTokenStore(context)
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    @Named("raw")
    fun provideBaseOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        firebaseAuth: FirebaseAuth,
        tokenStore: AuthTokenStore,
        authEventBus: AuthEventBus
    ): AuthInterceptor {
        return AuthInterceptor(firebaseAuth, tokenStore, authEventBus)
    }

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthenticatedOkHttpClient(
        @Named("raw") baseClient: OkHttpClient,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor(authInterceptor)
            .build()
    }
}
