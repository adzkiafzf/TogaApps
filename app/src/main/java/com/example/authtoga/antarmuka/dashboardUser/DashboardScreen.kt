package com.example.authtoga.antarmuka.dashboardUser

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.authtoga.data.model.Treatment
import com.example.authtoga.viewmodel.DashboardViewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape

// Definisi Warna Kode agar mirip Website TOGA
val TogaGreenDark = Color(0xFF1E5631)
val TogaGreenMedium = Color(0xFF2C6B46)
val TogaGreenLight = Color(0xFFE8F5E9)
val TogaBackground = Color(0xFFF9FBFA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToCatalog: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {}
) {

    val state = viewModel.uiState
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true, // Beranda aktif saat ini
                    onClick = { /* Tetap di halaman ini */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TogaGreenDark,
                        indicatorColor = TogaGreenLight
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToCatalog,
                    icon = { Icon(Icons.Default.List, contentDescription = "Tanaman") },
                    label = { Text("Tanaman", fontSize = 12.sp) }
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TogaBackground)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ================= 1. BANNER HEADER HIJAU MELENGKUNG =================
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(TogaGreenDark, TogaGreenMedium)
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Baris Atas: Nama Aplikasi & Tombol Profil Nindy
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌱 TOGA",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable { onNavigateToProfile() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!state.profilePictureUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = state.profilePictureUrl,
                                            contentDescription = "Foto Profil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.AccountCircle),
                                            placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.AccountCircle)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Profil Default",
                                            tint = Color.White,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Jargon Web TOGA
                            Text(
                                text = "Alam Menyembuhkan,\nIlmu Menjelaskan",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Jelajahi ratusan tanaman obat tradisional lengkap dengan khasiat dan cara penyajian.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Kolom Search Global
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Cari tanaman, penyakit...", color = Color.Gray, fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TogaGreenDark) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent
                                )
                            )
                        }
                    }
                }

                // ================= 2. SEKSI STATISTIK RINGKAS =================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(count = "6", label = "Tanaman", modifier = Modifier.weight(1f))
                        StatCard(count = "2", label = "User Aktif", modifier = Modifier.weight(1f))
                        StatCard(count = "1", label = "Tanggapan", modifier = Modifier.weight(1f))
                    }
                }

                // ================= 3. SEKSI CARA PENYAJIAN TERBARU =================
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Cara Penyajian Terbaru",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TogaGreenDark
                        )
                        Text(
                            text = "Ramuan herbal tradisional terpopuler",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (state.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TogaGreenMedium)
                        }
                    }
                } else if (state.errorMessage != null) {
                    item {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else if (state.recentTreatments.isEmpty()) {
                    item {
                        Text(
                            text = "Belum ada ramuan obat.",
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                } else {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(state.recentTreatments) { treatment ->
                                TreatmentWebStyleCard(
                                    treatment = treatment,
                                    onClick = { treatment.id?.let { onNavigateToDetail(it) } }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(count: String, label: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TogaGreenDark)
            Text(text = label, fontSize = 11.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreatmentWebStyleCard(treatment: Treatment, onClick: () -> Unit) {

    // KUNCI AMAN RELASI DATABASE BARU: Menembak URL Supabase via angka plant_id (contoh: 1.jpg, 2.jpg)
    val plantImageUrl = if (treatment.plant_id != null) {
        "${com.example.authtoga.data.SupabaseClient.SUPABASE_URL}/storage/v1/object/public/plant-images/${treatment.plant_id}.jpg"
    } else {
        ""
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Bagian Tempat Gambar Tanaman Obat
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(TogaGreenLight),
                contentAlignment = Alignment.Center
            ) {
                if (plantImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = plantImageUrl,
                        contentDescription = "Gambar Tanaman",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Search),
                        placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Search)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TogaGreenMedium,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Bagian Informasi Teks
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = treatment.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TogaGreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Khasiat:",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                Text(
                    text = treatment.disease_target,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TogaGreenMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Tombol aksi mini hijau
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TogaGreenDark)
                        // KUNCI TAMBAHAN: Kita tempel clickable agar klik pada tombol juga memicu aksi buka detail halaman
                        .clickable { onClick() }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Lihat Detail", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}