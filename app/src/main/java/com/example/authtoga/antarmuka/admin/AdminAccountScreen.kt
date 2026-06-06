package com.example.authtoga.antarmuka.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import com.example.authtoga.R
import com.example.authtoga.viewmodel.AdminAccount
import com.example.authtoga.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAccountScreen(
    currentEmail: String,
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val adminList by viewModel.adminList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var showTambahDialog by remember { mutableStateOf(false) }
    var showHapusDialog by remember { mutableStateOf<AdminAccount?>(null) }
    var namaBaru by remember { mutableStateOf("") }
    var emailBaru by remember { mutableStateOf("") }
    var passwordBaru by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadAdmins() }

    LaunchedEffect(message) {
        message?.let { viewModel.clearMessage() }
    }
    if (showTambahDialog) {
        AlertDialog(
            onDismissRequest = { showTambahDialog = false; namaBaru = ""; emailBaru = ""; passwordBaru = "" },
            title = { Text("Tambah Admin Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = namaBaru,
                        onValueChange = { namaBaru = it },
                        label = { Text("Nama Admin") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = emailBaru,
                        onValueChange = { emailBaru = it },
                        label = { Text("Email Admin (@toga.com)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = passwordBaru,
                        onValueChange = { passwordBaru = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        if (passwordVisible) R.drawable.outline_visibility_24
                                        else R.drawable.baseline_visibility_off_24
                                    ),
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.tambahAdmin(namaBaru, emailBaru, passwordBaru)
                        showTambahDialog = false
                        namaBaru = ""; emailBaru = ""; passwordBaru = ""
                    },
                    enabled = namaBaru.isNotBlank() && emailBaru.isNotBlank() && passwordBaru.isNotBlank()
                ) {
                    Text("Buat Baru", color = Color(0xFF1E5631), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTambahDialog = false; namaBaru = ""; emailBaru = ""; passwordBaru = "" }) {
                    Text("Batal")
                }
            }
        )
    }

    showHapusDialog?.let { admin ->
        AlertDialog(
            onDismissRequest = { showHapusDialog = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Admin") },
            text = { Text("Yakin ingin menghapus akun admin \"${admin.nama}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.hapusAdmin(admin.email); showHapusDialog = null }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHapusDialog = null }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Akun Admin") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FBFA))
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Info kamu sebagai admin
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E5631)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🪴", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Anda login sebagai Admin", fontSize = 12.sp, color = Color(0xFF1E5631))
                            Text(currentEmail, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E5631))
                        }
                    }
                }
            }

            // Tombol tambah admin
            item {
                Button(
                    onClick = { showTambahDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5631))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Admin")
                }
            }

            // Header list
            item {
                Text(
                    "Daftar Akun Admin",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E5631)
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF1E5631))
                    }
                }
            } else {
                items(adminList) { admin ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🪴", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(admin.nama, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFF1DA1F2))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(admin.email, fontSize = 12.sp, color = Color.Gray)
                            }
                            if (admin.email != currentEmail) {
                                IconButton(onClick = { showHapusDialog = admin }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            message?.let {
                item {
                    Text(it, color = Color(0xFF1E5631), fontSize = 13.sp, modifier = Modifier.padding(4.dp))
                }
            }
        }
    }
}
