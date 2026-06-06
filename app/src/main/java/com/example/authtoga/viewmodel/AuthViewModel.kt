package com.example.authtoga.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authtoga.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class Feedback(
    val user_email: String,
    val tampilkan_username: Boolean,
    val tentang: String,
    val detail: String
)

@Serializable
data class ProfileUpsert(
    val email: String,
    val display_name: String,
    val avatar_url: String
)

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentEmail = MutableStateFlow("")
    val currentEmail: StateFlow<String> = _currentEmail

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _editNama = MutableStateFlow("")
    val editNama: StateFlow<String> = _editNama

    private val _profileUpdateState = MutableStateFlow<String?>(null)
    val profileUpdateState: StateFlow<String?> = _profileUpdateState

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri

    private val _pendingPhotoUri = MutableStateFlow<Uri?>(null)
    val pendingPhotoUri: StateFlow<Uri?> = _pendingPhotoUri

    private val _avatarUrl = MutableStateFlow<String?>(null)
    val avatarUrl: StateFlow<String?> = _avatarUrl

    private val _feedbackState = MutableStateFlow<String?>(null)
    val feedbackState: StateFlow<String?> = _feedbackState

    fun login(email: String, password: String) {
        val domain = email.substringAfterLast("@")
        if (domain !in listOf("gmail.com", "toga.com")) {
            _authState.value = AuthState.Error("Email atau Password salah")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _currentEmail.value = email
                loadDisplayName(email)
                _authState.value = AuthState.Success("Login Berhasil!", email)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Email atau Password salah")
            }
        }
    }

    private suspend fun loadDisplayName(email: String) {
        try {
            val user = SupabaseClient.client.auth.retrieveUserForCurrentSession(updateSession = true)
            val displayName = user.userMetadata
                ?.get("display_name")
                ?.jsonPrimitive?.content
                ?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")
            val avatarUrl = user.userMetadata
                ?.get("avatar_url")
                ?.jsonPrimitive?.content
                ?.takeIf { it.isNotBlank() } ?: ""
            _userName.value = displayName
            _editNama.value = displayName
            _avatarUrl.value = avatarUrl.ifBlank { null }
            try {
                SupabaseClient.client.postgrest["profiles"].upsert(
                    ProfileUpsert(
                        email = email,
                        display_name = displayName,
                        avatar_url = avatarUrl.substringBefore("?")
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("PROFILE_SYNC", "upsert login error: ${e.message}")
            }
        } catch (e: Exception) {
            _userName.value = email.substringBefore("@")
            _editNama.value = _userName.value
        }
    }

    fun setEditNama(nama: String) {
        _editNama.value = nama
    }

    fun pilihFoto(uri: Uri) {
        _pendingPhotoUri.value = uri
    }

    fun resetPendingFoto() {
        _pendingPhotoUri.value = null
    }

    fun simpanSemuaPerubahan(newNama: String, context: Context) {
        viewModelScope.launch {
            try {
                val pendingUri = _pendingPhotoUri.value
                if (pendingUri != null) {
                    val email = _currentEmail.value
                    val fileName = "avatar_${email.replace("@", "_").replace(".", "_")}.jpg"
                    val bytes = context.contentResolver.openInputStream(pendingUri)?.readBytes()
                    if (bytes != null) {
                        SupabaseClient.client.storage["avatars"].upload(path = fileName, data = bytes) { upsert = true }
                        val publicUrl = SupabaseClient.client.storage["avatars"].publicUrl(fileName) + "?t=${System.currentTimeMillis()}"
                        SupabaseClient.client.auth.updateUser { data { put("avatar_url", JsonPrimitive(publicUrl)) } }
                        _avatarUrl.value = publicUrl
                        _photoUri.value = pendingUri
                        _pendingPhotoUri.value = null
                    }
                }
                SupabaseClient.client.auth.updateUser { data { put("display_name", JsonPrimitive(newNama)) } }
                SupabaseClient.client.postgrest["profiles"].upsert(
                    ProfileUpsert(
                        email = _currentEmail.value,
                        display_name = newNama,
                        avatar_url = _avatarUrl.value?.substringBefore("?") ?: ""
                    )
                )
                _userName.value = newNama
                _profileUpdateState.value = "Profil berhasil diperbarui"
            } catch (e: Exception) {
                _profileUpdateState.value = "Gagal menyimpan: ${e.localizedMessage}"
            }
        }
    }

    fun updateNama(newNama: String) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.updateUser {
                    data { put("display_name", JsonPrimitive(newNama)) }
                }
                SupabaseClient.client.postgrest["profiles"].upsert(
                    ProfileUpsert(
                        email = _currentEmail.value,
                        display_name = newNama,
                        avatar_url = _avatarUrl.value?.substringBefore("?") ?: ""
                    )
                )
                _userName.value = newNama
                _profileUpdateState.value = "Profil berhasil diperbarui"
            } catch (e: Exception) {
                _profileUpdateState.value = "Gagal menyimpan: ${e.localizedMessage}"
            }
        }
    }

    fun updatePhoto(uri: Uri, context: Context) {
        _photoUri.value = uri
        viewModelScope.launch {
            try {
                val email = _currentEmail.value
                val fileName = "avatar_${email.replace("@", "_").replace(".", "_")}.jpg"
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                SupabaseClient.client.storage["avatars"].upload(path = fileName, data = bytes) { upsert = true }
                val publicUrl = SupabaseClient.client.storage["avatars"].publicUrl(fileName) + "?t=${System.currentTimeMillis()}"
                SupabaseClient.client.auth.updateUser { data { put("avatar_url", JsonPrimitive(publicUrl)) } }
                _avatarUrl.value = publicUrl
                SupabaseClient.client.postgrest["profiles"].upsert(
                    ProfileUpsert(
                        email = _currentEmail.value,
                        display_name = _userName.value,
                        avatar_url = publicUrl.substringBefore("?")
                    )
                )
            } catch (e: Exception) {
                android.util.Log.e("PHOTO", "error: ${e.message}", e)
            }
        }
    }

    fun resetProfileUpdateState() {
        _profileUpdateState.value = null
    }

    fun register(email: String, password: String) {
        val domain = email.substringAfterLast("@")
        if (domain != "gmail.com") {
            _authState.value = AuthState.Error("Registrasi hanya untuk akun Gmail")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                _authState.value = AuthState.Success("Registrasi Berhasil! Silakan coba login.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Terjadi kesalahan saat registrasi")
            }
        }
    }

    fun kirimFeedback(tampilkanUsername: Boolean, tentang: String, detail: String) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.postgrest["feedback"].insert(
                    Feedback(
                        user_email = _currentEmail.value,
                        tampilkan_username = tampilkanUsername,
                        tentang = tentang,
                        detail = detail
                    )
                )
                _feedbackState.value = "Tanggapan anda berhasil terkirim"
            } catch (e: Exception) {
                _feedbackState.value = "Gagal mengirim: ${e.localizedMessage}"
            }
        }
    }

    fun resetFeedbackState() {
        _feedbackState.value = null
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                // ignore
            } finally {
                _authState.value = AuthState.Idle
                _currentEmail.value = ""
                _userName.value = ""
                _editNama.value = ""
                _photoUri.value = null
                _pendingPhotoUri.value = null
                _avatarUrl.value = null
            }
        }
    }
}
