package com.example.authtoga.antarmuka.admin

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
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
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTreatmentFormScreen(
    plantId: Int,
    namaTanaman: String,
    treatmentId: String? = null, // 1. TAMBAHKAN PARAMETER INI: Null berarti tambah baru, ada isinya berarti mode edit
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var resepTitle by remember { mutableStateOf("") }
    var diseaseTarget by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) } // Menyimpan url gambar lama saat edit
    var isSaving by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) selectedImageUri = uri }

    // 2. KUNCI UTAMA EDIT: Jika dalam mode edit, tarik data lama dari Supabase untuk dipasang ke form
    LaunchedEffect(treatmentId) {
        if (treatmentId != null) {
            try {
                val dataLama = SupabaseClient.client.postgrest["treatments"]
                    .select { filter { eq("id", treatmentId) } }
                    .decodeSingle<Treatment>()

                resepTitle = dataLama.title
                diseaseTarget = dataLama.disease_target
                ingredients = dataLama.ingredients
                instructions = dataLama.instructions

                // Ambil public URL gambar lama jika ID resep valid
                existingImageUrl = "${SupabaseClient.client.storage["treatment-images"].publicUrl(treatmentId)}.jpg"
            } catch (e: Exception) {
                println("Gagal memuat data lama: ${e.localizedMessage}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (treatmentId == null) "Tambah Resep $namaTanaman" else "Edit Resep $namaTanaman",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Kembali") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Box Preview Gambar Masakan Resep Jamu
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        // Jalur 1: Menampilkan gambar baru yang baru saja dipilih dari galeri
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!existingImageUrl.isNullOrBlank()) {
                        // Jalur 2: Menampilkan gambar lama yang sudah ada di storage Supabase
                        AsyncImage(
                            model = existingImageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🍵", fontSize = 36.sp)
                    }
                }
                IconButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Create, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Text("Pilih Foto Hasil Olahan Jamu Matang", fontSize = 12.sp, color = Color.Gray)

            OutlinedTextField(
                value = resepTitle,
                onValueChange = { resepTitle = it },
                label = { Text("Nama Menu Olahan (Contoh: Seduhan Kunyit Madu)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = diseaseTarget,
                onValueChange = { diseaseTarget = it },
                label = { Text("Target Keluhan Penyakit (Contoh: Nyeri Lambung)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text("Bahan-Bahan (Pisahkan dengan tanda koma)") },
                placeholder = { Text("Kunyit 2 ruas, Madu 1 sdm, Air 200ml") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Langkah-Langkah Penyajian (Gunakan baris baru)") },
                placeholder = { Text("1. Rebus kunyit...\n2. Tuang madu...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (resepTitle.isBlank() || diseaseTarget.isBlank()) {
                        Toast.makeText(context, "Nama resep & target khasiat wajib diisi!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSaving = true
                    coroutineScope.launch {
                        try {
                            // 3. LOGIKA UNGGAH GAMBAR BERDASARKAN ID RESEP
                            // Jika tambah baru, buat nama dari timestamp. Jika edit, timpa file dengan nama ID resep yang lama.
                            val targetIdName = treatmentId ?: "recipe_${System.currentTimeMillis()}"

                            if (selectedImageUri != null) {
                                val bytes = context.contentResolver.openInputStream(selectedImageUri!!).use { it?.readBytes() }
                                if (bytes != null) {
                                    val pathName = "$targetIdName.jpg"
                                    SupabaseClient.client.storage["treatment-images"].upload(path = pathName, data = bytes) { upsert = true }
                                }
                            }

                            // 4. SUSUN OBJEK UNTUK DISIMPAN
                            val newTreatment = Treatment(
                                id = treatmentId, // Jika diisi ID lama, Supabase tahu ini perintah pembaruan
                                title = resepTitle,
                                disease_target = diseaseTarget,
                                ingredients = ingredients,
                                instructions = instructions,
                                plant_id = plantId
                            )

                            // 5. KONDISI EKSEKUSI DATABASE: Pisah jalur Insert dan Update
                            if (treatmentId == null) {
                                SupabaseClient.client.postgrest["treatments"].insert(newTreatment)
                                Toast.makeText(context, "Resep berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                            } else {
                                SupabaseClient.client.postgrest["treatments"].update(newTreatment) {
                                    filter { eq("id", treatmentId) }
                                }
                                Toast.makeText(context, "Resep berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            }

                            isSaving = false
                            onBack()
                        } catch (e: Exception) {
                            isSaving = false
                            launch {
                                Toast.makeText(context, "Eror Database: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                            println("GAGAL SIMPAN RESEP: ${e.localizedMessage}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                } else {
                    Text(
                        text = if (treatmentId == null) "Simpan Resep Olahan" else "Perbarui Resep",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}