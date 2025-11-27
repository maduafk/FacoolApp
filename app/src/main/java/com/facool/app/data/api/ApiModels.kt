package com.facool.app.data.api

import com.google.gson.annotations.SerializedName

// Modelos para a API de fotos (usando Picsum Photos - API pública)
data class PhotoResponse(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String? = null,
    @SerializedName("download_url")
    val downloadUrl: String
)

