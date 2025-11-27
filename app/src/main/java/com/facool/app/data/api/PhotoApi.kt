package com.facool.app.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotoApi {
    // API pública de exemplo: Picsum Photos
    @GET("v2/list")
    suspend fun getPhotos(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<List<PhotoResponse>>
    
    // Alternativa: JSONPlaceholder para fotos
    @GET("photos")
    suspend fun getPhotosFromJsonPlaceholder(
        @Query("_limit") limit: Int = 20
    ): Response<List<PhotoResponse>>
}

