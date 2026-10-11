package com.servicedesk.app.di

import com.servicedesk.app.BuildConfig
import com.servicedesk.app.data.api.AnalyticsApi
import com.servicedesk.app.data.api.AuthApi
import com.servicedesk.app.data.api.MediaApi
import com.servicedesk.app.data.api.MessagesApi
import com.servicedesk.app.data.api.StaffApi
import com.servicedesk.app.data.api.TicketsApi
import com.servicedesk.app.data.api.TicketsExportApi
import com.servicedesk.app.data.api.UsersApi
import com.servicedesk.app.data.remote.TokenInterceptor
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenInterceptor: TokenInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val baseUrl = BuildConfig.SERVER_BASE_URL.let { if (it.endsWith("/")) it else "$it/" }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideTicketsApi(retrofit: Retrofit): TicketsApi = retrofit.create(TicketsApi::class.java)

    @Provides
    @Singleton
    fun provideUsersApi(retrofit: Retrofit): UsersApi = retrofit.create(UsersApi::class.java)

    @Provides
    @Singleton
    fun provideStaffApi(retrofit: Retrofit): StaffApi = retrofit.create(StaffApi::class.java)

    @Provides
    @Singleton
    fun provideMessagesApi(retrofit: Retrofit): MessagesApi = retrofit.create(MessagesApi::class.java)

    @Provides
    @Singleton
    fun provideMediaApi(retrofit: Retrofit): MediaApi = retrofit.create(MediaApi::class.java)

    @Provides
    @Singleton
    fun provideAnalyticsApi(retrofit: Retrofit): AnalyticsApi = retrofit.create(AnalyticsApi::class.java)

    @Provides
    @Singleton
    fun provideTicketsExportApi(retrofit: Retrofit): TicketsExportApi =
        retrofit.create(TicketsExportApi::class.java)
}
