package com.example.authtoga.antarmuka.user.dashboardUser

import com.example.authtoga.data.model.Treatment

data class DashboardState(
    val isLoading: Boolean = false,
    val userName: String = "Nindy",
    val profilePictureUrl: String? = null, // Tambahkan baris penampung URL ini
    val recentTreatments: List<Treatment> = emptyList(),
    val errorMessage: String? = null
)
