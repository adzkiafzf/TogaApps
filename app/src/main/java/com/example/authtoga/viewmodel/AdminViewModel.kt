package com.example.authtoga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authtoga.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminAccount(
    val nama: String,
    val email: String
)

class AdminViewModel : ViewModel() {

    private val _adminList = MutableStateFlow<List<AdminAccount>>(emptyList())
    val adminList: StateFlow<List<AdminAccount>> = _adminList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private suspend fun ensureAdminAuth() {
        SupabaseClient.adminClient.auth.importAuthToken(SupabaseClient.SUPABASE_SERVICE_KEY)
    }

    fun loadAdmins() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ensureAdminAuth()
                val profiles = SupabaseClient.adminClient.postgrest["profiles"]
                    .select {
                        filter { like("email", "%@toga.com") }
                    }
                    .decodeList<ProfileUpsert>()
                _adminList.value = profiles.map { AdminAccount(nama = it.display_name.ifBlank { it.email.substringBefore("@") }, email = it.email) }
            } catch (e: Exception) {
                _message.value = "Gagal memuat data: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun tambahAdmin(nama: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ensureAdminAuth()
                SupabaseClient.adminClient.auth.admin.createUserWithEmail {
                    this.email = email
                    this.password = password
                    this.autoConfirm = true
                    this.userMetadata = buildJsonObject {
                        put("display_name", JsonPrimitive(nama))
                    }
                }
                SupabaseClient.adminClient.postgrest["profiles"].upsert(
                    ProfileUpsert(email = email, display_name = nama, avatar_url = "")
                )
                _message.value = "Admin \"$nama\" berhasil ditambahkan."
                loadAdmins()
            } catch (e: Exception) {
                _message.value = "Gagal tambah admin: ${e.localizedMessage}"
                android.util.Log.e("ADMIN", "tambahAdmin error", e)
                _isLoading.value = false
            }
        }
    }

    fun hapusAdmin(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ensureAdminAuth()
                val users = SupabaseClient.adminClient.auth.admin.retrieveUsers(page = 1, perPage = 1000)
                val user = users.find { it.email == email }
                if (user != null) {
                    SupabaseClient.adminClient.auth.admin.deleteUser(user.id)
                }
                SupabaseClient.client.postgrest["profiles"]
                    .delete { filter { eq("email", email) } }
                _adminList.value = _adminList.value.filter { it.email != email }
                _message.value = "Admin berhasil dihapus"
            } catch (e: Exception) {
                _message.value = "Gagal hapus: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
