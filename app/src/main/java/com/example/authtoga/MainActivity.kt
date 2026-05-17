package com.example.authtoga

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.authtoga.ui.theme.AuthTogaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuthTogaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = { navController.navigate("register") },
                                onLoginSuccess = { email ->
                                    val destination = if (email.endsWith("@toga.com")) "admin" else "user"
                                    navController.navigate(destination) {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(onNavigateToLogin = { navController.popBackStack() })
                        }
                        composable("admin") {
                            AdminScreen(onLogout = {
                                navController.navigate("login") {
                                    popUpTo("admin") { inclusive = true }
                                }
                            })
                        }
                        composable("user") {
                            UserScreen(onLogout = {
                                navController.navigate("login") {
                                    popUpTo("user") { inclusive = true }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
