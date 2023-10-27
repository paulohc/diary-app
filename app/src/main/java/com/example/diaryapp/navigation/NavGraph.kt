package com.example.diaryapp.navigation

import androidx.compose.runtime.*
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import com.example.auth.navigation.authenticationRoute
import com.example.home.navigation.homeRoute
import com.example.util.Screen
import com.example.write.navigation.writeRoute

@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit,
) {
    NavHost(
        startDestination = startDestination,
        navController = navController,
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.buildRoute())
            },
            onDataLoaded = onDataLoaded,
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.buildRoute())
            },
            navigateToWriteWithArgs = {
                navController.navigate(Screen.Write.passDiaryId(diaryId = it))
            },
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.buildRoute())
            },
            onDataLoaded = onDataLoaded,
        )
        writeRoute(
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }
}