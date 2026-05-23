package com.example.authtoga.antarmuka

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.authtoga.viewmodel.AuthViewModel

@Composable
fun UserScreen(
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onFeedback: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val email by viewModel.currentEmail.collectAsState()
    val nama by viewModel.userName.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val displayImage: Any? = photoUri ?: avatarUrl

    val displayNama = nama.ifBlank { "userXT001" }
    val displayEmail = email.ifBlank { "user@gmail.com" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            // Foto profil
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (displayImage != null) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = "Foto Profil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Foto Profil",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = displayNama, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = displayEmail, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            MenuCard(icon = Icons.Default.Edit, label = "Edit Profil", onClick = onEditProfile)
            Spacer(modifier = Modifier.height(12.dp))
            MenuCard(icon = Icons.Default.Star, label = "Tanggapan Aplikasi", onClick = onFeedback)
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Button(
                onClick = { viewModel.logout(); onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar")
            }
        }
    }
}

@Composable
private fun MenuCard(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
