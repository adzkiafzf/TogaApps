package com.example.authtoga.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authtoga.antarmuka.dashboardUser.DashboardState
import com.example.authtoga.data.PlantRepository
import com.example.authtoga.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    var uiState by mutableStateOf(DashboardState())
        private set

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                // 1. AMBIL DATA REMEDY/TREATMENT DARI PLANTREPOSITORY (Ini yang bikin eror merah tadi!)
                val treatments = PlantRepository.getRecentTreatments()

                // 2. Ambil info email user aktif dari Supabase Auth
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val userEmail = user?.email ?: ""
                val displayName = if (userEmail.isNotBlank()) userEmail.substringBefore("@") else "Pengguna TOGA"

                // 3. Format nama file gambar sesuai format Edit Profile kelompokmu
                var avatarUrl: String? = null
                if (userEmail.isNotBlank()) {
                    try {
                        val formattedEmailName = userEmail.replace("@", "_").replace(".", "_")
                        val fileName = "avatar_$formattedEmailName.jpg"

                        avatarUrl = SupabaseClient.client.storage.from("avatars")
                            .publicUrl(fileName)
                    } catch (e: Exception) {
                        // Jika gagal mengambil URL, biarkan null
                    }
                }

                // 4. Masukkan semua data ke uiState (Gunakan huruf kecil 'uiState')
                uiState = uiState.copy(
                    isLoading = false,
                    userName = displayName,
                    profilePictureUrl = avatarUrl,
                    recentTreatments = treatments
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Gagal memuat data: ${e.localizedMessage}"
                )
            }
        }
    }
}