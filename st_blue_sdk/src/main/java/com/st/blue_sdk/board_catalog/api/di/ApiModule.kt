/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.board_catalog.api.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.st.blue_sdk.BuildConfig
import com.st.blue_sdk.board_catalog.api.BoardCatalogApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    private const val ST_TIMEOUT: Long = 30

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val contentType = "application/json".toMediaType()

    @RetrofitBasePath
    @Provides
    fun provideApiBasePath() = BuildConfig.BLUESTSDK_DB_BASE_URL

    @Provides
    fun provideHttpLoggingInterceptorLevel(): HttpLoggingInterceptor.Level =
        if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.BASIC

    @Provides
    fun provideLoggingInterceptor(loggingLevel: HttpLoggingInterceptor.Level): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            setLevel(loggingLevel)
        }

    @Provides
    fun provideHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder().apply {
        callTimeout(ST_TIMEOUT, TimeUnit.SECONDS)
        addInterceptor(loggingInterceptor)
    }.build()

    @Provides
    fun provideRetrofitClient(
        httpClient: OkHttpClient,
        @RetrofitBasePath basePath: String,
        converterFactory: Converter.Factory
    ): Retrofit = Retrofit.Builder()
        .client(httpClient)
        .baseUrl(basePath)
        .addConverterFactory(converterFactory)
        .build()

    @Provides
    fun provideBordCatalogApi(retrofitClient: Retrofit): BoardCatalogApi =
        retrofitClient.create(BoardCatalogApi::class.java)

    @Provides
    fun provideJsonSerDer(): Json {
        return json
    }

    @Provides
    fun provideConverterFactory(): Converter.Factory =
        json.asConverterFactory(contentType = contentType)
}
