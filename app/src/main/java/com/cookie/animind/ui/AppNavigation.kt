package com.cookie.animind.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cookie.animind.ui.screens.ChatScreen
import com.cookie.animind.ui.screens.DetailScreen
import com.cookie.animind.ui.screens.HomeScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: AnimeViewModel
) {
    NavHost(
        navController = navController, 
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onAnimeClick = {
                    navController.navigate("detail")
                },
                onSettingsClick = {
                    // Handled internally by state swap
                }
            )
        }
        composable("detail") {
            DetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onChatClick = { navController.navigate("chat") }
            )
        }
        composable("chat") {
            ChatScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
