package com.example.authtoga.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authtoga.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// hanya field yang ada di tabel feedback
@Serializable
data class FeedbackRow(
    val id: String? = null,
    val user_email: String = "",
    val tampilkan_username: Boolean = true,
    val tentang: String = "",
    val detail: String = "",
    val status: String = "Baru",
    val created_at: String? = null
)

// untuk UI — gabungan feedback + profil terbaru
data class FeedbackItem(
    val id: String? = null,
    val user_email: String = "",
    val tampilkan_username: Boolean = true,
    val tentang: String = "",
    val detail: String = "",
    val status: String = "Baru",
    val resolvedName: String = "",
    val resolvedAvatar: String = ""
)

@Serializable
data class ProfileRow(
    val email: String = "",
    val display_name: String = "",
    val avatar_url: String = ""
)

@Serializable
data class StatusUpdate(@SerialName("status") val status: String)

class FeedbackViewModel : ViewModel() {

    private val _feedbacks = MutableStateFlow<List<FeedbackItem>>(emptyList())
    val feedbacks: StateFlow<List<FeedbackItem>> = _feedbacks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg

    fun clearFeedbacks() {
        _feedbacks.value = emptyList()
        _errorMsg.value = null
    }

    fun loadFeedbacks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val rows = SupabaseClient.client.postgrest["feedback"]
                    .select()
                    .decodeList<FeedbackRow>()

                val profiles = SupabaseClient.client.postgrest["profiles"]
                    .select()
                    .decodeList<ProfileRow>()
                    .associateBy { it.email }

                _feedbacks.value = rows.map { row ->
                    val profile = profiles[row.user_email]
                    android.util.Log.d("ADMIN", "email=${row.user_email} name=${profile?.display_name} avatar=${profile?.avatar_url}")
                    FeedbackItem(
                        id = row.id,
                        user_email = row.user_email,
                        tampilkan_username = row.tampilkan_username,
                        tentang = row.tentang,
                        detail = row.detail,
                        status = row.status,
                        resolvedName = profile?.display_name?.ifBlank { row.user_email.substringBefore("@") }
                            ?: row.user_email.substringBefore("@"),
                        resolvedAvatar = profile?.avatar_url ?: ""
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ADMIN", "loadFeedbacks error: ${e.message}", e)
                _errorMsg.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.postgrest["feedback"].update(
                    StatusUpdate("Dibaca")
                ) { filter { eq("id", id) } }
                _feedbacks.value = _feedbacks.value.map {
                    if (it.id == id) it.copy(status = "Dibaca") else it
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message
            }
        }
    }

    fun deleteFeedback(id: String) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.postgrest["feedback"].delete {
                    filter { eq("id", id) }
                }
                _feedbacks.value = _feedbacks.value.filter { it.id != id }
            } catch (e: Exception) {
                _errorMsg.value = e.message
            }
        }
    }
}
