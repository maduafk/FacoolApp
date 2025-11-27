package com.facool.app.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.facool.app.data.api.NetworkModule
import com.facool.app.data.api.PhotoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PhotosUiState(
    val photos: List<PhotoResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PhotosViewModel(app: Application) : AndroidViewModel(app) {
    private val photoApi = NetworkModule.photoApi
    
    private val _uiState = MutableStateFlow(PhotosUiState())
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()
    
    init {
        loadPhotos()
    }
    
    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = photoApi.getPhotos(page = 1, limit = 20)
                if (response.isSuccessful) {
                    val photos = response.body() ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        photos = photos,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Erro ao carregar fotos: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro de conexão: ${e.message}"
                )
            }
        }
    }
    
    fun refreshPhotos() {
        loadPhotos()
    }
}

