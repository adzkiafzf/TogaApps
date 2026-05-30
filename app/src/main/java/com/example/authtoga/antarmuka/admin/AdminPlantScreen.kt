package com.example.authtoga.antarmuka.admin

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.authtoga.data.Plant
import com.example.authtoga.viewmodel.PlantState
import com.example.authtoga.viewmodel.PlantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlantScreen(
    onBack: () -> Unit,
    onAddPlant: () -> Unit,
    onEditPlant: (Plant) -> Unit,
    onPlantClick: (Plant) -> Unit, // Parameter baru untuk mendeteksi klik pada container tanaman
    viewModel: PlantViewModel = viewModel()
) {
    val plants by viewModel.plants.collectAsState()
    val state by viewModel.state.collectAsState()
    var deleteTarget by remember { mutableStateOf<Plant?>(null) }

    LaunchedEffect(state) {
        if (state is PlantState.Success) viewModel.resetState()
    }

    deleteTarget?.let { plant ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Hapus Tanaman") },
            text = { Text("Hapus \"${plant.nama_tanaman}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlant(plant.id, plant.gambar_url ?: "")
                    deleteTarget = null
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Tanaman") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlant) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tanaman")
            }
        }
    ) { padding ->
        when {
            state is PlantState.Loading && plants.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            plants.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada tanaman. Tap + untuk menambahkan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(plants, key = { it.id }) { plant ->
                        PlantCard(
                            plant = plant,
                            onEdit = { onEditPlant(plant) },
                            onDelete = { deleteTarget = plant },
                            onClick = { onPlantClick(plant) } // Card diklik mengarah ke detail resep
                        )
                    }
                }
            }
        }

        if (state is PlantState.Error) {
            LaunchedEffect(state) {
                viewModel.resetState()
            }
        }
    }
}

@Composable
private fun PlantCard(
    plant: Plant,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Aksi ketika container item ditekan
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (plant.gambar_url?.isNotBlank() == true) {
                AsyncImage(
                    model = plant.gambar_url,
                    contentDescription = plant.nama_tanaman,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(plant.nama_tanaman, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(plant.kategori, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    plant.khasiat.take(60) + if (plant.khasiat.length > 60) "…" else "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tombol Edit Data Tanaman Induk
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            // Tombol Hapus Data Tanaman Induk
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}