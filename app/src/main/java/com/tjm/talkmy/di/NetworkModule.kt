package com.tjm.talkmy.di

import com.tjm.oroscapp.data.core.interceptors.AuthInterceptor
import com.tjm.talkmy.data.network.TaskApiService
import com.tjm.talkmy.data.repositoriesImp.TasksOnlineRepositoryImp
import com.tjm.talkmy.domain.repositories.TasksOnlineRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import com.tjm.talkmy.BuildConfig.BASE_URL


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetorfit(okHttpClient: OkHttpClient): Retrofit{
        return Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideokHttpClient(authInterceptor: AuthInterceptor):OkHttpClient{
        val interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient
            .Builder()
            .addInterceptor(interceptor)
            .addInterceptor(authInterceptor)
            .build()
    }
    @Provides
    @Singleton
    fun provideHoroscopeApiservice(retrofit: Retrofit):TaskApiService{
        return retrofit.create(TaskApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(apiService: TaskApiService): TasksOnlineRepository {
        return TasksOnlineRepositoryImp(apiService)
    }
}