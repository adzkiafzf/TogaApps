package com.example.authtoga.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Treatment(
    val id: String? = null,
    // UBAH BARIS INI: Tambahkan tanda tanya (?) setelah String dan berikan nilai default = null
    val plant_id: Int? = null,
    val title: String,
    val disease_target: String,
    val ingredients: String,
    val instructions: String,
    val created_at: String? = null,
    val created_by: String? = null
)