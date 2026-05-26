package com.example.authtoga.antarmuka

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.authtoga.data.Plant
import com.example.authtoga.viewmodel.PlantState
import com.example.authtoga.viewmodel.PlantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPlantFormScreen(
    editPlant: Plant? = null,
    onBack: () -> Unit,
    viewModel: PlantViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val uploadedUrl by viewModel.uploadedImageUrl.collectAsState()

    val nama by viewModel.formNama.collectAsState()
    val deskripsi by viewModel.formDeskripsi.collectAsState()
    val khasiat by viewModel.formKhasiat.collectAsState()
    val kategori by viewModel.formKategori.collectAsState()
    val gambarUrl by viewModel.formGambarUrl.collectAsState()

    // Isi form saat pertama buka (mode tambah = kosong, sudah dihandle clearEditTarget)
    LaunchedEffect(editPlant?.id) {
        if (editPlant == null) {
            viewModel.formNama.value = ""
            viewModel.formDeskripsi.value = ""
            viewModel.formKhasiat.value = ""
            viewModel.formKategori.value = ""
            viewModel.formGambarUrl.value = ""
        }
    }

    // Saat upload selesai, update gambarUrl
    LaunchedEffect(uploadedUrl) {
        if (uploadedUrl.isNotBlank()) {
            viewModel.formGambarUrl.value = uploadedUrl
            viewModel.resetUploadedUrl()
        }
    }

    LaunchedEffect(state) {
        if (state is PlantState.Success) {
            viewModel.resetState()
            onBack()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadImage(it, context) }
    }

    val isLoading = state is PlantState.Loading
    val title = if (editPlant == null) "Tambah Tanaman" else "Edit Tanaman"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Foto tanaman
            Box(contentAlignment = Alignment.BottomEnd) {
                val displayImage: Any? = gambarUrl.takeIf { it.isNotBlank() }
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (displayImage != null) {
                        AsyncImage(
                            model = displayImage,
                            contentDescription = "Foto Tanaman",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("🌿", style = MaterialTheme.typography.displaySmall)
                    }
                }
                IconButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.Create,
                        contentDescription = "Ganti Foto",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (isLoading && gambarUrl.isBlank()) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(20.dp))

            listOf(
                Triple("Nama Tanaman", nama) { v: String -> viewModel.formNama.value = v },
                Triple("Kategori", kategori) { v: String -> viewModel.formKategori.value = v },
                Triple("Khasiat", khasiat) { v: String -> viewModel.formKhasiat.value = v },
                Triple("Deskripsi", deskripsi) { v: String -> viewModel.formDeskripsi.value = v }
            ).forEach { (label, value, onValueChange) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = label != "Deskripsi" && label != "Khasiat",
                    minLines = if (label == "Deskripsi" || label == "Khasiat") 2 else 1
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(8.dp))

            if (state is PlantState.Error) {
                Text(
                    (state as PlantState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    viewModel.savePlant(editPlant?.id, nama, deskripsi, khasiat, kategori, gambarUrl)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && nama.isNotBlank()
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Simpan")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
