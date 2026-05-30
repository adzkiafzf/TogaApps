package com.example.authtoga.antarmuka.detailTanamanUser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.authtoga.data.SupabaseClient
import com.example.authtoga.data.model.Treatment
import com.example.authtoga.viewmodel.DetailViewModel
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

val TogaGreenDark = Color(0xFF1E5631)
val TogaGreenMedium = Color(0xFF2C6B46)
val TogaGreenLight = Color(0xFFE8F5E9)
val TogaBackground = Color(0xFFF4F7F5)
val TogaOrangeAccent = Color(0xFFE67E22)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    treatmentId: String,
    onNavigateBack: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val state = viewModel.uiState

    // FIXED: Cadangan State Lokal jika data di ViewModel tidak sinkron / kosong
    var localTreatment by remember { mutableStateOf<Treatment?>(null) }
    var isLocalLoading by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    // Jalankan pencarian data real-time langsung dari database Supabase
    LaunchedEffect(treatmentId) {
        // Tetap jalankan bawaan ViewModel kalian sebagai backup awal
        viewModel.loadTreatmentDetail(treatmentId)

        // Ambil data murni bypass dari Postgrest Supabase berdasarkan UUID
        coroutineScope.launch {
            try {
                isLocalLoading = true
                val dataRealtime = SupabaseClient.client.postgrest["treatments"]
                    .select {
                        filter { eq("id", treatmentId) }
                    }.decodeSingle<Treatment>()

                localTreatment = dataRealtime
                isLocalLoading = false
            } catch (e: Exception) {
                isLocalLoading = false
                localErrorMessage = "Gagal memuat resep dari database: ${e.localizedMessage}"
                println("DETAIL REALTIME EROR: ${e.localizedMessage}")
            }
        }
    }

    // Menentukan data mana yang dipakai (Utamakan data real-time lokal agar tidak 'Ramuan tidak ditemukan')
    val activeTreatment = localTreatment ?: state.treatment
    val isLoadingActive = if (localTreatment != null) false else (state.isLoading || isLocalLoading)
    val errorActive = if (localTreatment != null) null else (state.errorMessage ?: localErrorMessage)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TogaBackground)
    ) {
        if (isLoadingActive) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = TogaGreenMedium
            )
        } else if (errorActive != null && activeTreatment == null) {
            Text(
                text = errorActive,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else if (activeTreatment == null) {
            // fallback terakhir jika benar-benar kosong total
            Text(
                text = "Ramuan tidak ditemukan",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            activeTreatment.let { treatment ->
                val treatmentImageUrl = if (!treatment.id.isNullOrBlank()) {
                    "${SupabaseClient.SUPABASE_URL}/storage/v1/object/public/treatment-images/${treatment.id}.jpg"
                } else {
                    ""
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    // ================= 1. FOTO PRODUK / TANAMAN OBAT =================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        AsyncImage(
                            model = treatmentImageUrl,
                            contentDescription = "Foto Sajian Ramuan",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // ================= 2. KONTEN UTAMA (GAYA LEMBARAN MELENGKUNG) =================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-20).dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color.White)
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.5f))
                                .align(Alignment.CenterHorizontally)
                        )

                        // NAMA RAMUAN HERBAL
                        Text(
                            text = treatment.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            fontFamily = FontFamily.Serif,
                            color = TogaGreenDark,
                            lineHeight = 36.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // SPANDUK INFO KHASIAT UTAMA
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TogaGreenDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Khasiat Utama: ",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = treatment.disease_target,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 2.dp))

                        // ================= SEKSI BAHAN-BAHAN =================
                        Text(
                            text = "Bahan-Bahan yang Diperlukan",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TogaGreenDark
                        )

                        val ingredientsList = treatment.ingredients.split(",").map { it.trim() }
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ingredientsList.forEach { ingredient ->
                                if (ingredient.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•  ",
                                            color = TogaGreenMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                        Text(
                                            text = ingredient,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF2C3E50),
                                            textAlign = TextAlign.Start
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // ================= SEKSI LANGKAH PENYAJIAN =================
                        Text(
                            text = "Langkah-Langkah Penyajian",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TogaGreenDark
                        )

                        val instructionsList = treatment.instructions.replace("\\n", "\n").split("\n").map { it.trim() }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            instructionsList.forEachIndexed { index, step ->
                                if (step.isNotBlank()) {
                                    val cleanStep = step.replace(Regex("^\\d+\\.\\s*"), "")

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 12.dp, top = 2.dp)
                                                .size(26.dp)
                                                .background(TogaOrangeAccent, shape = CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = (index + 1).toString(),
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = cleanStep,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF2C3E50),
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // ================= 3. FLOATING BACK BUTTON MINIMALIS =================
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp)
                .size(40.dp)
                .background(TogaGreenDark, shape = CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Kembali",
                tint = Color.White
            )
        }
    }
}