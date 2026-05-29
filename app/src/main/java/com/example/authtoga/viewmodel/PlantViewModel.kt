package com.example.authtoga.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authtoga.data.Plant
import com.example.authtoga.data.PlantRepository
import com.example.authtoga.data.PlantUpsert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PlantState {
    object Idle : PlantState()
    object Loading : PlantState()
    data class Success(val message: String) : PlantState()
    data class Error(val message: String) : PlantState()
}

class PlantViewModel : ViewModel() {

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants

    private val _state = MutableStateFlow<PlantState>(PlantState.Idle)
    val state: StateFlow<PlantState> = _state

    private val _uploadedImageUrl = MutableStateFlow("")
    val uploadedImageUrl: StateFlow<String> = _uploadedImageUrl

    private val _editTarget = MutableStateFlow<Plant?>(null)
    val editTarget: StateFlow<Plant?> = _editTarget

    // Form state — survive rotation karena ada di ViewModel
    val formNama = MutableStateFlow("")
    val formDeskripsi = MutableStateFlow("")
    val formKhasiat = MutableStateFlow("")
    val formKategori = MutableStateFlow("")
    val formGambarUrl = MutableStateFlow("")

    fun setEditTarget(plant: Plant) {
        _editTarget.value = plant
        formNama.value = plant.nama_tanaman
        formDeskripsi.value = plant.deskripsi
        formKhasiat.value = plant.khasiat
        formKategori.value = plant.kategori
        // PERBAIKAN DI BARIS 48: Tambahkan ?: "" di ujungnya
        formGambarUrl.value = plant.gambar_url ?: ""
    }

    fun clearEditTarget() {
        _editTarget.value = null
        formNama.value = ""
        formDeskripsi.value = ""
        formKhasiat.value = ""
        formKategori.value = ""
        formGambarUrl.value = ""
    }

    init { loadPlants() }

    fun loadPlants() {
        viewModelScope.launch {
            _state.value = PlantState.Loading
            try {
                _plants.value = PlantRepository.getAll()
                _state.value = PlantState.Idle
            } catch (e: Exception) {
                _state.value = PlantState.Error(e.localizedMessage ?: "Gagal memuat tanaman")
            }
        }
    }

    fun uploadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uploadedImageUrl.value = PlantRepository.uploadImage(uri, context)
            } catch (e: Exception) {
                _state.value = PlantState.Error("Gagal upload foto: ${e.localizedMessage}")
            }
        }
    }

    fun savePlant(id: Int?, nama: String, deskripsi: String, khasiat: String, kategori: String, gambarUrl: String) {
        viewModelScope.launch {
            _state.value = PlantState.Loading
            try {
                val upsert = PlantUpsert(nama, deskripsi, khasiat, kategori, gambarUrl)
                if (id == null) PlantRepository.insert(upsert)
                else PlantRepository.update(id, upsert)
                _state.value = PlantState.Success(if (id == null) "Tanaman berhasil ditambahkan" else "Tanaman berhasil diperbarui")
                loadPlants()
            } catch (e: Exception) {
                _state.value = PlantState.Error(e.localizedMessage ?: "Gagal menyimpan")
            }
        }
    }

    fun deletePlant(id: Int, gambarUrl: String) {
        viewModelScope.launch {
            _state.value = PlantState.Loading
            try {
                PlantRepository.delete(id, gambarUrl)
                _state.value = PlantState.Success("Tanaman berhasil dihapus")
                loadPlants()
            } catch (e: Exception) {
                _state.value = PlantState.Error(e.localizedMessage ?: "Gagal menghapus")
            }
        }
    }

    fun resetState() { _state.value = PlantState.Idle }
    fun resetUploadedUrl() { _uploadedImageUrl.value = "" }
}
