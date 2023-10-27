package com.example.diaryapp.navigation

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.diaryapp.BuildConfig
import com.example.diaryapp.data.repository.MongoDB
import com.example.diaryapp.model.*
import com.example.diaryapp.presentation.components.DisplayAlertDialog
import com.example.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.example.diaryapp.presentation.screens.auth.AuthenticationViewModel
import com.example.diaryapp.presentation.screens.home.HomeScreen
import com.example.diaryapp.presentation.screens.home.HomeViewModel
import com.example.diaryapp.presentation.screens.write.WriteScreen
import com.example.diaryapp.presentation.screens.write.WriteViewModel
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.*

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

fun NavGraphBuilder.composable(
    screen: Screen,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(route = screen.route, arguments = screen.arguments, content = content)
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    composable(screen = Screen.Authentication) {
        val viewModel: AuthenticationViewModel = viewModel()
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(Unit) {
            onDataLoaded()
        }

        AuthenticationScreen(
            state = viewModel.state,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            },
            onSuccessfulFirebaseSignIn = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                        viewModel.setLoading(false)
                    }
                )
            },
            onFailedFirebaseSignIn = {
                messageBarState.addError(it)
                viewModel.setLoading(false)
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    composable(screen = Screen.Home) {
        val viewModel: HomeViewModel = viewModel()
        val diaries by viewModel.diaries
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember { mutableStateOf(false) }

        LaunchedEffect(diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteWithArgs = navigateToWriteWithArgs,
        )

        LaunchedEffect(Unit) {
            MongoDB.configureTheReam()
        }

        DisplayAlertDialog(
            title = "Sign Out",
            message = "Are you sure you want to Sign Out from your Google Account?",
            dialogOpened = signOutDialogOpened,
            onDialogClosed = { signOutDialogOpened = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(BuildConfig.APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        withContext(Dispatchers.Main) {
                            navigateToAuth()
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun NavGraphBuilder.writeRoute(
    onBackPressed: () -> Unit,
) {
    composable(screen = Screen.Write) {
        val context = LocalContext.current
        val viewModel: WriteViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val galleryState = viewModel.galleryState
        val pagerState = rememberPagerState(pageCount = { Mood.values().size })
        val pageNumber by remember { derivedStateOf { pagerState.currentPage } }

        WriteScreen(
            uiState = uiState,
            moodName = { Mood.values()[pageNumber].name },
            pagerState = pagerState,
            galleryState = galleryState,
            onTitleChanged = viewModel::setTitle,
            onDescriptionChanged = viewModel::setDescription,
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackPressed()
                    },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onDateTimeUpdated = viewModel::updateTime,
            onBackPressed = onBackPressed,
            onSaveClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = { onBackPressed() },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                )
            },
            onImageSelect = {
                val type = context.contentResolver.getType(it)
                    ?.split("/")?.last() ?: "jpg"
                Log.d("WriteViewModel", "uri: $it")
                viewModel.addImage(image = it, imageType = type)
            },
            onImageDeleteClicked = {

            },
        )
    }
}
