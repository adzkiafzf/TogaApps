package com.example.authtoga.viewmodel


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authtoga.antarmuka.detailTanamanUser.DetailState
import com.example.authtoga.data.PlantRepository
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    var uiState by mutableStateOf(DetailState())
        private set

    // Fungsi untuk memuat data ramuan berdasarkan ID unik dari Supabase
    fun loadTreatmentDetail(treatmentId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                // Ambil semua daftar ramuan, lalu cari yang ID-nya cocok
                val allTreatments = PlantRepository.getRecentTreatments()
                val matchedTreatment = allTreatments.find { it.id == treatmentId }

                if (matchedTreatment != null) {
                    uiState = uiState.copy(isLoading = false, treatment = matchedTreatment)
                } else {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Ramuan tidak ditemukan")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat detail: ${e.localizedMessage}"
                )
            }
        }
    }
}