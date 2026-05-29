package com.example.authtoga.antarmuka.detailTanamanUser


import com.example.authtoga.data.model.Treatment

data class DetailState(
    val isLoading: Boolean = false,
    val treatment: Treatment? = null,
    val errorMessage: String? = null
)