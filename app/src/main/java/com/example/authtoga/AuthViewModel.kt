package com.example.authtoga

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String, val email: String = "") : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

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
                _authState.value = AuthState.Success("Login Berhasil!", email)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Email atau Password salah")
            }
        }
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
            }
        }
    }
}