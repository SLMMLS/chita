package com.lector.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lector.app.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Reader : Screen("reader/{bookId}") {
        fun createRoute(bookId: Long) = "reader/$bookId"
    }
    object Search : Screen("search")
    object Settings : Screen("settings")
}

@Composable
fun LectorNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLibrary = { /* TODO: Navigate to Library */ },
                onNavigateToSearch = { /* TODO: Navigate to Search */ }
            )
        }
        // Остальные экраны добавим на следующих этапах
    }
}