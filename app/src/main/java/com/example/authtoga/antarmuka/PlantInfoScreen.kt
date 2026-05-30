package com.example.authtoga.antarmuka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.authtoga.data.Plant
import com.example.authtoga.data.PlantRepository
import com.example.authtoga.data.SupabaseClient
import com.example.authtoga.data.model.Treatment
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

private val TogaGreenDark = Color(0xFF1E5631)
private val TogaGreenMedium = Color(0xFF2C6B46)
private val TogaGreenLight = Color(0xFFE8F5E9)
private val TogaBackground = Color(0xFFF4F7F5)
private val TogaOrangeAccent = Color(0xFFE67E22)
private val TogaTextDark = Color(0xFF2C3E50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantInfoScreen(
    plantId: Int,
    onNavigateBack: () -> Unit,
    onNavigateToTreatmentDetail: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // State untuk data tanaman induk
    var plant by remember { mutableStateOf<Plant?>(null) }
    var isLoadingPlant by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // FIXED: State lokal untuk menampung resep khusus tanaman ini langsung dari Supabase
    var associatedTreatments by remember { mutableStateOf<List<Treatment>>(emptyList()) }
    var isLoadingTreatments by remember { mutableStateOf(true) }

    // Ambil data tanaman dan resep secara bersamaan saat halaman dibuka
    LaunchedEffect(plantId) {
        coroutineScope.launch {
            try {
                isLoadingPlant = true
                val allPlants = PlantRepository.getAll()
                plant = allPlants.find { it.id == plantId }
                isLoadingPlant = false
            } catch (e: Exception) {
                isLoadingPlant = false
                errorMessage = "Gagal memuat informasi tanaman: ${e.localizedMessage}"
            }
        }

        // FIXED: Query mandiri langsung ke tabel treatments berdasarkan plantId murni (Aman & Real-time)
        coroutineScope.launch {
            try {
                isLoadingTreatments = true
                associatedTreatments = SupabaseClient.client.postgrest["treatments"]
                    .select(columns = Columns.ALL) {
                        filter { eq("plant_id", plantId) }
                    }.decodeList<Treatment>()
                isLoadingTreatments = false
            } catch (e: Exception) {
                isLoadingTreatments = false
                println("User gagal memuat resep: ${e.localizedMessage}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TogaBackground)
    ) {
        // FIXED: Loading akan terus berputar sampai data tanaman DAN resep selesai diunduh
        if (isLoadingPlant || isLoadingTreatments) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = TogaGreenMedium
            )
        } else if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Center).padding(16.dp)
            )
        } else {
            plant?.let { currentPlant ->
                val plantImageUrl = currentPlant.gambar_url ?: ""

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ================= 1. FOTO UTAMA REAL TANAMAN OBAT =================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        AsyncImage(
                            model = plantImageUrl,
                            contentDescription = currentPlant.nama_tanaman,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.08f))
                        )
                    }

                    // ================= 2. LEMBARAN KONTEN UTAMA MELENGKUNG =================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-20).dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color.White)
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray.copy(alpha = 0.5f))
                                .align(Alignment.CenterHorizontally)
                        )

                        // NAMA TANAMAN
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = currentPlant.nama_tanaman,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = TogaGreenDark
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = TogaGreenLight.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = "${currentPlant.kategori}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TogaGreenMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        // SEKSI 1: DESKRIPSI TANAMAN
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Deskripsi Tanaman",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TogaGreenDark
                            )
                            Text(
                                text = currentPlant.deskripsi,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TogaTextDark,
                                textAlign = TextAlign.Justify,
                                lineHeight = 22.sp
                            )
                        }

                        // SEKSI 2: KHASIAT TANAMAN
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Khasiat & Manfaat Alami",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TogaGreenDark
                            )
                            Text(
                                text = currentPlant.khasiat ?: "Informasi khasiat belum tersedia.",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = TogaTextDark,
                                textAlign = TextAlign.Start,
                                lineHeight = 22.sp
                            )
                        }

                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                        // SEKSI 3: JENIS OLAHAN / CARA PENYAJIAN RESEP (Dapat Di-klik)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Pilihan Menu Olahan Resep",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TogaGreenDark
                            )
                            Text(
                                text = "Silakan pilih ramuan di bawah untuk melihat langkah penyajiannya:",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            if (associatedTreatments.isEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = TogaBackground),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Belum ada menu olahan resep penyajian untuk tanaman ini.",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(16.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                associatedTreatments.forEach { treatment ->
                                    val treatmentImageUrl = "${SupabaseClient.SUPABASE_URL}/storage/v1/object/public/treatment-images/${treatment.id}.jpg"

                                    Card(
                                        onClick = { treatment.id?.let { onNavigateToTreatmentDetail(it) } },
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = treatmentImageUrl,
                                                contentDescription = treatment.title,
                                                modifier = Modifier
                                                    .size(60.dp)
                                                    .clip(RoundedCornerShape(10.dp)),
                                                contentScale = ContentScale.Crop
                                            )

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = treatment.title,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TogaGreenDark
                                                )
                                                Text(
                                                    text = "Keluhan: ${treatment.disease_target}",
                                                    fontSize = 12.sp,
                                                    color = TogaOrangeAccent,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Lihat Resep",
                                                tint = TogaGreenMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        // ================= 3. FLOATING BACK BUTTON =================
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