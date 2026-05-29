package com.example.authtoga.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.authtoga.antarmuka.AdminFeedbackListScreen
import com.example.authtoga.antarmuka.AdminPlantFormScreen
import com.example.authtoga.antarmuka.AdminPlantScreen
import com.example.authtoga.antarmuka.AdminScreen
import com.example.authtoga.antarmuka.EditProfileScreen
import com.example.authtoga.antarmuka.FeedbackScreen
import com.example.authtoga.antarmuka.LoginScreen
import com.example.authtoga.antarmuka.RegisterScreen
import com.example.authtoga.antarmuka.UserScreen
import com.example.authtoga.antarmuka.dashboardUser.DashboardScreen
import com.example.authtoga.antarmuka.PlantCatalogScreen // Import halaman katalog baru
import com.example.authtoga.viewmodel.AuthViewModel
import com.example.authtoga.viewmodel.PlantViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.authtoga.antarmuka.detailTanamanUser.DetailScreen
import com.example.authtoga.antarmuka.PlantInfoScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val plantViewModel: PlantViewModel = viewModel()
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
                onBack = { navController.popBackStack() },
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
        composable(Screen.ADMIN_PLANT) {
            AdminPlantScreen(
                onBack = { navController.popBackStack() },
                onAddPlant = { navController.navigate(Screen.ADMIN_PLANT_FORM) },
                onEditPlant = { plant ->
                    plantViewModel.setEditTarget(plant)
                    navController.navigate(Screen.ADMIN_PLANT_FORM)
                },
                viewModel = plantViewModel
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
                onNavigateToProfile = { navController.navigate(Screen.EDIT_PROFILE) },
                onNavigateToFeedback = { navController.navigate(Screen.FEEDBACK) },
                // PERBAIKAN: Klik tombol "Tanaman" di navbar sekarang mengarah ke Katalog Baru!
                onNavigateToCatalog = { navController.navigate(Screen.PLANT_CATALOG) },
                onNavigateToDetail = { treatmentId ->
                    navController.navigate("detail_screen/$treatmentId")
                }
            )
        }

        // ================= RUTE KATALOG TANAMAN BARU NINDY =================
//        composable(Screen.PLANT_CATALOG) {
//            PlantCatalogScreen(
//                onNavigateToHome = {
//                    navController.navigate(Screen.DASHBOARD) {
//                        popUpTo(Screen.DASHBOARD) { inclusive = true }
//                    }
//                },
//                onNavigateToFeedback = { navController.navigate(Screen.FEEDBACK) },
//                onNavigateToProfile = { navController.navigate(Screen.EDIT_PROFILE) },
//                onNavigateToDetail = { treatmentId ->
//                    navController.navigate("detail_screen/$treatmentId")
//                }
//            )
//        }

        // ================= SEKSI DETAIL RAMUAN =================
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

        // DAFTARKAN SEKSI KATALOG TANAMAN INDUK DI APPNAVIGATION KELOMPOKMU
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
                    // Lempar ID bertipe angka murni (Int) ke halaman detail tanaman induk baru kalian
                    navController.navigate("plant_info_screen/$plantId")
                }
            )
        }

        // ================= RUTE BARU DETAIL INFORMASI TANAMAN INDUK NINDY =================
        composable(
            route = "plant_info_screen/{plantId}",
            arguments = listOf(navArgument("plantId") { type = NavType.IntType }) // Menangkap ID integer tanaman induk
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getInt("plantId") ?: 0
            PlantInfoScreen(
                plantId = plantId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTreatmentDetail = { treatmentId ->
                    // Ketika resep diklik, otomatis melempar user menuju halaman cara penyajian!
                    navController.navigate("detail_screen/$treatmentId")
                }
            )
        }
    }
}