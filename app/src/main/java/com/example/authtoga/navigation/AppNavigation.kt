package com.example.authtoga.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.authtoga.antarmuka.AdminScreen
import com.example.authtoga.antarmuka.LoginScreen
import com.example.authtoga.antarmuka.RegisterScreen
import com.example.authtoga.antarmuka.UserScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.LOGIN) {
        composable(Screen.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.REGISTER) },
                onLoginSuccess = { email ->
                    val destination = if (email.endsWith("@toga.com")) Screen.ADMIN else Screen.USER
                    navController.navigate(destination) {
                        popUpTo(Screen.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.REGISTER) {
            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
        }
        composable(Screen.ADMIN) {
            AdminScreen(onLogout = {
                navController.navigate(Screen.LOGIN) {
                    popUpTo(Screen.ADMIN) { inclusive = true }
                }
            })
        }
        composable(Screen.USER) {
            UserScreen(onLogout = {
                navController.navigate(Screen.LOGIN) {
                    popUpTo(Screen.USER) { inclusive = true }
                }
            })
        }
    }
}
