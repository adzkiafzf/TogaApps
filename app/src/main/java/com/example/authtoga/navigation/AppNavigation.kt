package com.example.authtoga.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.authtoga.antarmuka.AdminFeedbackListScreen
import com.example.authtoga.antarmuka.AdminScreen
import com.example.authtoga.antarmuka.EditProfileScreen
import com.example.authtoga.antarmuka.FeedbackScreen
import com.example.authtoga.antarmuka.LoginScreen
import com.example.authtoga.antarmuka.RegisterScreen
import com.example.authtoga.antarmuka.UserScreen
import com.example.authtoga.viewmodel.AuthViewModel
import com.example.authtoga.viewmodel.FeedbackViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.LOGIN) {
        composable(Screen.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.REGISTER) },
                onLoginSuccess = { email ->
                    val destination = if (email.endsWith("@toga.com")) Screen.ADMIN else Screen.USER
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
    }
}
