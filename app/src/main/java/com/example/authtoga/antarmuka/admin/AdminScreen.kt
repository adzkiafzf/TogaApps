package com.example.authtoga.antarmuka.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.authtoga.viewmodel.AuthViewModel

@Composable
fun AdminScreen(
    onLogout: () -> Unit,
    onKelolaTanggapan: () -> Unit,
    onKelolaTanaman: () -> Unit = {},
    onKelolaAkun: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val email by viewModel.currentEmail.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FBFA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))

            // Foto profil admin (dummy dengan warna berbeda)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🪴",
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nama admin + badge verifikasi
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Admin Toga",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF1DA1F2))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = email.ifBlank { "admin@toga.com" },
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            AdminMenuCard(
                icon = Icons.Default.Star,
                label = "Kelola Tanggapan User",
                onClick = onKelolaTanggapan
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminMenuCard(
                icon = Icons.Default.List,
                label = "Kelola Tanaman",
                onClick = onKelolaTanaman
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminMenuCard(
                icon = Icons.Default.AccountCircle,
                label = "Kelola Akun Admin",
                onClick = onKelolaAkun
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Button(
                onClick = { viewModel.logout(); onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5631))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar")
            }
        }
    }
}

@Composable
fun AdminMenuCard(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color(0xFF1E5631))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
