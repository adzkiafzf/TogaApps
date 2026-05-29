package com.example.authtoga.antarmuka

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.authtoga.data.Plant
import com.example.authtoga.data.PlantRepository
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val TogaGreenDark = Color(0xFF1E5631)
private val TogaGreenMedium = Color(0xFF2C6B46)
private val TogaGreenLight = Color(0xFFE8F5E9)
private val TogaBackground = Color(0xFFF4F7F5)
private val TogaTextDark = Color(0xFF2C3E50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantCatalogScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPlantDetail: (Int) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var plantList by remember { mutableStateOf<List<Plant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var catalogSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                plantList = PlantRepository.getAll()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Gagal memuat data: ${e.localizedMessage}"
            }
        }
    }

    val filteredPlants = plantList.filter {
        it.nama_tanaman.contains(catalogSearchQuery, ignoreCase = true) ||
                (it.khasiat ?: "").contains(catalogSearchQuery, ignoreCase = true)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.List, contentDescription = "Tanaman") },
                    label = { Text("Tanaman", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TogaGreenDark,
                        indicatorColor = TogaGreenLight
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToFeedback,
                    icon = { Icon(Icons.Default.Email, contentDescription = "Tanggapan") },
                    label = { Text("Tanggapan", fontSize = 12.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil", fontSize = 12.sp) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(TogaBackground)
                .padding(innerPadding)
        ) {
            // ================= BANNER HEADER HIJAU MELENGKUNG PREMIUM =================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(TogaGreenDark, TogaGreenMedium)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🌿 Katalog Tanaman Obat",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Daftar tanaman herbal kaya khasiat alami",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Kolom Pencarian Khusus di dalam Banner Header
                    OutlinedTextField(
                        value = catalogSearchQuery,
                        onValueChange = { catalogSearchQuery = it },
                        placeholder = { Text("Cari nama tanaman atau khasiat...", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TogaGreenDark) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TogaGreenMedium)
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (filteredPlants.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Tanaman tidak ditemukan.", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPlants) { plant ->

                        // FIX: Membangun kembali URL gambar tanaman asli dari folder 'plant-images'
                        val plantImageUrl = if (plant.id != 0) {
                            "${com.example.authtoga.data.SupabaseClient.SUPABASE_URL}/storage/v1/object/public/plant-images/${plant.id}.jpg"
                        } else {
                            ""
                        }

                        Card(
                            onClick = { onNavigateToPlantDetail(plant.id) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .background(TogaGreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (plantImageUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = plantImageUrl,
                                            contentDescription = plant.nama_tanaman,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = TogaGreenMedium)
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = plant.nama_tanaman,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TogaGreenDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = TogaGreenLight.copy(alpha = 0.7f)),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.wrapContentSize()
                                    ) {
                                        Text(
                                            text = plant.kategori,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TogaGreenMedium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Manfaat / Khasiat:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )

                                    Text(
                                        text = plant.khasiat ?: "Khasiat belum diisi",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TogaTextDark,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}