package xyz.zephr.demo.di

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.zephr.demo.BuildConfig
import xyz.zephr.demo.data.api.PlacesApiService
import xyz.zephr.demo.data.local.AuthTokenStore
import xyz.zephr.demo.data.network.AuthEventBus
import xyz.zephr.demo.data.network.AuthInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .serializeNulls()
            .setLenient()
            .disableHtmlEscaping()
            .create()
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
    @Named("auth")
    fun provideAuthenticatedOkHttpClient(
        @Named("raw") baseClient: OkHttpClient,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return baseClient.newBuilder()
            .addInterceptor(authInterceptor)
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
    fun provideAuthenticatedRetrofit(
        @Named("auth") okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        val baseUrl = "${BuildConfig.API_BASE_URL}/v1/"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun providePlacesApiService(@Named("auth") retrofit: Retrofit): PlacesApiService {
        return retrofit.create(PlacesApiService::class.java)
    }
}
