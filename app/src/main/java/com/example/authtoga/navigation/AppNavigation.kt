package com.example.authtoga.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.authtoga.antarmuka.admin.AdminAccountScreen
import com.example.authtoga.antarmuka.admin.AdminFeedbackListScreenimport com.example.authtoga.antarmuka.admin.AdminPlantFormScreen
import com.example.authtoga.antarmuka.admin.AdminPlantScreen
import com.example.authtoga.antarmuka.admin.AdminScreen
import com.example.authtoga.antarmuka.EditProfileScreen
import com.example.authtoga.antarmuka.FeedbackScreen
import com.example.authtoga.antarmuka.LoginScreen
import com.example.authtoga.antarmuka.RegisterScreen
import com.example.authtoga.antarmuka.UserScreen
import com.example.authtoga.antarmuka.user.dashboardUser.DashboardScreen
import com.example.authtoga.antarmuka.PlantCatalogScreen
import com.example.authtoga.viewmodel.AuthViewModel
import com.example.authtoga.viewmodel.PlantViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.authtoga.antarmuka.detailTanamanUser.DetailScreen
import com.example.authtoga.antarmuka.PlantInfoScreen
import com.example.authtoga.antarmuka.admin.AdminPlantDetailScreen
import com.example.authtoga.antarmuka.admin.AdminTreatmentFormScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val plantViewModel: PlantViewModel = viewModel()
    val dashboardViewModel: com.example.authtoga.viewmodel.DashboardViewModel = viewModel()
    val editTarget by plantViewModel.editTarget.collectAsState()

    NavHost(navController = navController, startDestination = Screen.LOGIN) {
        composable(Screen.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.REGISTER) },
                onLoginSuccess = { email ->
                    val destination = if (email.endsWith("@toga.com")) Screen.ADMIN else Screen.DASHBOARD
                    navController.navigate(destination) {
                        popUpTo(Screen.LOGIN) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        composable(Screen.ADMIN) {
            AdminScreen(
                onLogout = {
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(Screen.ADMIN) { inclusive = true }
                    }
                },
                onKelolaTanggapan = { navController.navigate(Screen.ADMIN_FEEDBACK) },
                onKelolaTanaman = { navController.navigate(Screen.ADMIN_PLANT) },
                onKelolaAkun = { navController.navigate(Screen.ADMIN_ACCOUNT) },
                viewModel = authViewModel
            )
        }

        composable(Screen.USER) {
            UserScreen(
                onLogout = {
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(Screen.USER) { inclusive = true }
                    }
                },
                onEditProfile = { navController.navigate(Screen.EDIT_PROFILE) },
                onFeedback = { navController.navigate(Screen.FEEDBACK) },
                viewModel = authViewModel
            )
        }

        composable(Screen.EDIT_PROFILE) {
            EditProfileScreen(
                onBack = {
                    dashboardViewModel.loadDashboardData()
                    navController.popBackStack()
                },
                onLogoutSuccess = {
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(Screen.DASHBOARD) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.FEEDBACK) {
            FeedbackScreen(
                onBack = { navController.popBackStack() },
                viewModel = authViewModel
            )
        }

        composable(Screen.ADMIN_FEEDBACK) {
            AdminFeedbackListScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.ADMIN_ACCOUNT) {
            val email by authViewModel.currentEmail.collectAsState()
            AdminAccountScreen(
                currentEmail = email,
                onBack = { navController.popBackStack() }
            )
        }

        // ================= 1. JALUR UTAMA KELOLA TANAMAN ADMIN =================
        composable(Screen.ADMIN_PLANT) {
            AdminPlantScreen(
                onBack = { navController.popBackStack() },
                onAddPlant = { navController.navigate(Screen.ADMIN_PLANT_FORM) },
                onEditPlant = { plant ->
                    plantViewModel.setEditTarget(plant)
                    navController.navigate(Screen.ADMIN_PLANT_FORM)
                },
                onPlantClick = { plant ->
                    // ALUR BARU: Klik container langsung diarahkan ke halaman detail resep admin!
                    navController.navigate("admin_plant_detail_screen/${plant.id}/${plant.nama_tanaman}")
                },
                viewModel = plantViewModel
            )
        }

        // ================= 2. REGISTER HALAMAN LIST DETAIL RESEP ADMIN =================
        composable(
            route = "admin_plant_detail_screen/{plantId}/{namaTanaman}",
            arguments = listOf(
                navArgument("plantId") { type = NavType.IntType },
                navArgument("namaTanaman") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
            val namaTanaman = backStackEntry.arguments?.getString("namaTanaman") ?: ""

            AdminPlantDetailScreen(
                plantId = plantId,
                namaTanaman = namaTanaman,
                onBack = { navController.popBackStack() },
                onAddTreatment = {
                    // Jika klik tombol +, buka form dengan mode tambah resep baru
                    navController.navigate("admin_treatment_form/$plantId/$namaTanaman")
                },
                onEditTreatment = { treatment ->
                    // Jika klik tombol edit resep, kita arahkan ke form resep sambil bawa ID resepnya!
                    navController.navigate("admin_treatment_form/$plantId/$namaTanaman?treatmentId=${treatment.id}")
                }
            )
        }

        // ================= 3. SESUAIKAN RUTE FORM RESEP AGAR BISA TERIMA MODE EDIT =================
        composable(
            route = "admin_treatment_form/{plantId}/{namaTanaman}?treatmentId={treatmentId}",
            arguments = listOf(
                navArgument("plantId") { type = NavType.IntType },
                navArgument("namaTanaman") { type = NavType.StringType },
                navArgument("treatmentId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
            val namaTanaman = backStackEntry.arguments?.getString("namaTanaman") ?: ""
            val treatmentId = backStackEntry.arguments?.getString("treatmentId")

            AdminTreatmentFormScreen(
                plantId = plantId,
                namaTanaman = namaTanaman,
                treatmentId = treatmentId, // Parameter opsional untuk tahu sedang mode edit atau tambah
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ADMIN_PLANT_FORM) {
            AdminPlantFormScreen(
                editPlant = editTarget,
                onBack = {
                    plantViewModel.clearEditTarget()
                    navController.popBackStack()
                },
                viewModel = plantViewModel
            )
        }

        // ================= SEKSI BERANDA / DASHBOARD =================
        composable(Screen.DASHBOARD) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToProfile = { navController.navigate(Screen.EDIT_PROFILE) },
                onNavigateToFeedback = { navController.navigate(Screen.FEEDBACK) },
                onNavigateToCatalog = { navController.navigate(Screen.PLANT_CATALOG) },
                onNavigateToDetail = { treatmentId ->
                    navController.navigate("detail_screen/$treatmentId")
                }
            )
        }

        // ================= SEKSI DETAIL RAMUAN USER =================
        composable(
            route = "detail_screen/{treatmentId}",
            arguments = listOf(navArgument("treatmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val treatmentId = backStackEntry.arguments?.getString("treatmentId") ?: ""
            DetailScreen(
                treatmentId = treatmentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ================= RUTE KATALOG TANAMAN USER =================
        composable(Screen.PLANT_CATALOG) {
            PlantCatalogScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.DASHBOARD) {
                        popUpTo(Screen.DASHBOARD) { inclusive = true }
                    }
                },
                onNavigateToFeedback = { navController.navigate(Screen.FEEDBACK) },
                onNavigateToProfile = { navController.navigate(Screen.EDIT_PROFILE) },
                onNavigateToPlantDetail = { plantId ->
                    navController.navigate("plant_info_screen/$plantId")
                }
            )
        }

        // ================= RUTE DETAIL INFORMASI TANAMAN INDUK USER =================
        composable(
            route = "plant_info_screen/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.IntType })
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
            PlantInfoScreen(
                plantId = plantId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTreatmentDetail = { treatmentId ->
                    navController.navigate("detail_screen/$treatmentId")
                }
            )
        }

        // ================= REGISTER RUTE FORM RESEP BARU ADMIN =================
        composable(
            route = "admin_treatment_form/{plantId}/{namaTanaman}",
            arguments = listOf(
                navArgument("plantId") { type = NavType.IntType },
                navArgument("namaTanaman") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
            val namaTanaman = backStackEntry.arguments?.getString("namaTanaman") ?: ""
            AdminTreatmentFormScreen(
                plantId = plantId, // FIX: Mengirim tipe Int murni sesuai parameter form resep terbaru
                namaTanaman = namaTanaman,
                onBack = { navController.popBackStack() }
            )
        }
    }
}