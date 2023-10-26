package com.example.diaryapp

import android.os.*
import androidx.activity.*
import androidx.activity.compose.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.*
import androidx.navigation.compose.*
import com.example.diaryapp.navigation.*
import com.example.diaryapp.ui.theme.*
import com.google.firebase.FirebaseApp
import io.realm.kotlin.mongodb.*

class MainActivity : ComponentActivity() {
    private var keepSplashOpened = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseApp.initializeApp(this)
        setContent {
            DiaryAppTheme(dynamicColor = false) {
                val navController = rememberNavController()
                SetupNavGraph(
                    startDestination = getStartDestination(),
                    navController = navController,
                    onDataLoaded = {
                        keepSplashOpened = false
                    }
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