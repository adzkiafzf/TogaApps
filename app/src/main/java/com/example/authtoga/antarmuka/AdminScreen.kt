package com.example.authtoga.antarmuka

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.authtoga.viewmodel.AuthViewModel

@Composable
fun AdminScreen(onLogout: () -> Unit, viewModel: AuthViewModel = viewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Halaman Admin", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Selamat datang, Admin!", color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            viewModel.logout()
            onLogout()
        }) {
            Text(text = "Logout")
        }
    }
}
