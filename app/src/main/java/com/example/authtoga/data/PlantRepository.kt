package com.example.authtoga.data

import android.content.Context
import android.net.Uri
import com.example.authtoga.data.model.Treatment
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable


@Serializable
data class Plant(
    val id: Int = 0,
    val nama_tanaman: String,
    val deskripsi: String,
    val khasiat: String,
    val kategori: String,
    val gambar_url: String = "",
    val created_at: String = ""
)

@Serializable
data class PlantUpsert(
    val nama_tanaman: String,
    val deskripsi: String,
    val khasiat: String,
    val kategori: String,
    val gambar_url: String
)

object PlantRepository {

    private val db get() = SupabaseClient.client.postgrest
    private val storage get() = SupabaseClient.client.storage["plants"]

    suspend fun getAll(): List<Plant> =
        db["plants"].select().decodeList()

    suspend fun insert(plant: PlantUpsert) {
        db["plants"].insert(plant)
    }

    suspend fun update(id: Int, plant: PlantUpsert) {
        db["plants"].update(plant) { filter { eq("id", id) } }
    }

    suspend fun delete(id: Int, gambarUrl: String) {
        if (gambarUrl.isNotBlank()) {
            val fileName = gambarUrl.substringAfterLast("/").substringBefore("?")
            runCatching { storage.delete(listOf(fileName)) }
        }
        db["plants"].delete { filter { eq("id", id) } }
    }

    suspend fun uploadImage(uri: Uri, context: Context): String {
        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            ?: error("Gagal membaca file")
        val fileName = "plant_${System.currentTimeMillis()}.jpg"
        storage.upload(path = fileName, data = bytes) { upsert = true }
        return storage.publicUrl(fileName)
    }

    // TAMBAHKAN INI DI DALAM KELAS PLANTREPOSITORY (Tepat sebelum tanda } penutup kelas)
    suspend fun getRecentTreatments(): List<Treatment> {
        // Kita ganti 'supabase' menjadi 'SupabaseClient.client' sesuai objek kalian
        return SupabaseClient.client.postgrest["treatments"]
            .select {
                limit(5)
            }.decodeList<Treatment>()
    }
}
