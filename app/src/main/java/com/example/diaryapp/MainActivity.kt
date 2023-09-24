package com.example.diaryapp

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.*
import androidx.navigation.compose.*
import com.example.diaryapp.navigation.*
import com.example.diaryapp.ui.theme.*
import io.realm.kotlin.mongodb.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DiaryAppTheme {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                )
            }
        }
    }
}

private fun getStartDestination(): String {
    val user = App.create(BuildConfig.APP_ID).currentUser
    return if (user?.loggedIn == true) {
        Screen.Home.buildRoute()
    } else {
        Screen.Authentication.buildRoute()
    }
}