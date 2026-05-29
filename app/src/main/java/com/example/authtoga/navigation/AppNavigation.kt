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
import com.example.authtoga.viewmodel.AuthViewModel
import com.example.authtoga.viewmodel.PlantViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.authtoga.antarmuka.detailTanamanUser.DetailScreen

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

        // SEKSI DASHBOARD
        composable(Screen.DASHBOARD) {
            DashboardScreen(
                onNavigateToProfile = { navController.navigate(Screen.EDIT_PROFILE) },
                onNavigateToFeedback = { navController.navigate(Screen.FEEDBACK) },
                onNavigateToCatalog = { navController.navigate(Screen.USER) },
                onNavigateToDetail = { treatmentId ->
                    // PERBAIKAN: Kirim argumen dengan pola path yang dikenali sistem internal Android secara konsisten
                    navController.navigate("detail_screen/$treatmentId")
                }
            )
        }

        // PERBAIKAN RUTE DETAIL: Menggunakan nama rute yang unik agar tidak bentrok saat restore state
        composable(
            route = "detail_screen/{treatmentId}",
            arguments = listOf(
                navArgument("treatmentId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val treatmentId = backStackEntry.arguments?.getString("treatmentId") ?: ""
            DetailScreen(
                treatmentId = treatmentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}