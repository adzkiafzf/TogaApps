package com.example.authtoga.antarmuka.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.authtoga.data.SupabaseClient
import com.example.authtoga.data.model.Treatment
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlantDetailScreen(
    plantId: Int,
    namaTanaman: String,
    onBack: () -> Unit,
    onAddTreatment: () -> Unit,
    onEditTreatment: (Treatment) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Menggunakan JsonObject sementara agar kebal dari eror crash deseralisasi model data class
    var rawTreatments by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadTreatments() {
        coroutineScope.launch {
            try {
                isLoading = true

                // Menarik data dalam bentuk JsonObject murni dari Supabase
                val response = SupabaseClient.client.postgrest["treatments"]
                    .select(columns = Columns.ALL) {
                        filter { eq("plant_id", plantId) }
                    }.decodeList<JsonObject>()

                rawTreatments = response
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                println("Gagal ambil resep: ${e.localizedMessage}")
                launch {
                    Toast.makeText(context, "Eror: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) { loadTreatments() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resep $namaTanaman", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTreatment,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Resep", tint = Color.White)
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (rawTreatments.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Belum ada resep untuk tanaman ini.", color = Color.Gray)
                    Text("ID Terdeteksi: $plantId", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(rawTreatments) { jsonItem ->
                    // Ambil data string dengan aman dari JSON mentah database
                    val idResep = jsonItem["id"]?.jsonPrimitive?.content ?: ""
                    val title = jsonItem["title"]?.jsonPrimitive?.content ?: "Tanpa Nama"
                    val target = jsonItem["disease_target"]?.jsonPrimitive?.content ?: "-"
                    val ingredients = jsonItem["ingredients"]?.jsonPrimitive?.content ?: ""
                    val instructions = jsonItem["instructions"]?.jsonPrimitive?.content ?: ""
                    val pId = jsonItem["plant_id"]?.jsonPrimitive?.int ?: plantId

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ambil URL public gambar langsung dari bucket berdasarkan ID Resep
                            val treatmentImageUrl = "${SupabaseClient.client.storage["treatment-images"].publicUrl(idResep)}.jpg"

                            AsyncImage(
                                model = treatmentImageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Khasiat: $target", fontSize = 12.sp, color = Color.Gray)
                            }

                            // Tombol Edit - Otomatis menyusun kembali objek Treatment yang sah untuk dilempar ke Form
                            IconButton(onClick = {
                                val treatmentObj = Treatment(
                                    id = idResep,
                                    title = title,
                                    disease_target = target,
                                    ingredients = ingredients,
                                    instructions = instructions,
                                    plant_id = pId
                                )
                                onEditTreatment(treatmentObj)
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            }

                            // Tombol Hapus
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    try {
                                        SupabaseClient.client.postgrest["treatments"].delete {
                                            filter { eq("id", idResep) }
                                        }
                                        Toast.makeText(context, "Resep berhasil dihapus", Toast.LENGTH_SHORT).show()
                                        loadTreatments()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal hapus: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}