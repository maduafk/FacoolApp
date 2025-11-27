package com.facool.app.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    // Base URL para Picsum Photos (API pública de imagens)
    private const val BASE_URL_PICSUM = "https://picsum.photos/"
    
    // Base URL alternativa: JSONPlaceholder
    private const val BASE_URL_JSONPLACEHOLDER = "https://jsonplaceholder.typicode.com/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofitPicsum = Retrofit.Builder()
        .baseUrl(BASE_URL_PICSUM)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val photoApi: PhotoApi = retrofitPicsum.create(PhotoApi::class.java)
}

