package com.example.diaryapp.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun SetupNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(
        startDestination = startDestination,
        navController = navController,
    ) {
        authenticationRoute()
        homeRoute()
        writeRoute()
    }
}

fun NavGraphBuilder.composable(
    screen: Screen,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(route = screen.route, arguments = screen.arguments, content = content)
}

fun NavGraphBuilder.authenticationRoute() {
    composable(screen = Screen.Authentication) {
    }
}

fun NavGraphBuilder.homeRoute() {
    composable(screen = Screen.Home) {
    }
}

fun NavGraphBuilder.writeRoute() {
    composable(screen = Screen.Write) {
    }
}
