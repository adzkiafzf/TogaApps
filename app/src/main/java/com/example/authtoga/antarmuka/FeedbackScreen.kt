package com.example.authtoga.antarmuka

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.authtoga.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val email by viewModel.currentEmail.collectAsState()
    val nama by viewModel.userName.collectAsState()
    val feedbackState by viewModel.feedbackState.collectAsState()

    var tampilkanUsername by remember { mutableStateOf(true) }
    var tentang by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }

    LaunchedEffect(feedbackState) {
        feedbackState?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetFeedbackState()
            if (!it.startsWith("Gagal")) onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tanggapan Aplikasi") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E5631),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FBFA))
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Card identitas pengirim
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF1E5631)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (tampilkanUsername) nama.ifBlank { "userXT001" } else "Anonim",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = email.ifBlank { "user@gmail.com" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tampilkan\nUsername", style = MaterialTheme.typography.labelSmall)
                        Switch(
                            checked = tampilkanUsername,
                            onCheckedChange = { tampilkanUsername = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Input Tentang (maks 15 karakter)
            OutlinedTextField(
                value = tentang,
                onValueChange = { if (it.length <= 15) tentang = it },
                label = { Text("Tentang") },
                supportingText = { Text("${tentang.length}/15") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Input Detail (maks 100 karakter)
            OutlinedTextField(
                value = detail,
                onValueChange = { if (it.length <= 100) detail = it },
                label = { Text("Detail") },
                supportingText = { Text("${detail.length}/100") },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.kirimFeedback(tampilkanUsername, tentang, detail) },
                modifier = Modifier.fillMaxWidth(),
                enabled = tentang.isNotBlank() && detail.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5631))
            ) {
                Text("Kirim")
            }
        }
    }
}
