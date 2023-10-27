package com.example.diaryapp.presentation.screens.home

import android.annotation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.example.diaryapp.R
import com.example.diaryapp.data.repository.Diaries
import com.example.diaryapp.model.RequestState
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    diaries: Diaries,
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    dateIsSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit,
) {
    var padding by remember { mutableStateOf(PaddingValues()) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked,
        onDeleteAllClicked = onDeleteAllClicked,
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                HomeTopBar(
                    scrollBehavior = scrollBehavior,
                    onMenuClicked = onMenuClicked,
                    dateIsSelected = dateIsSelected,
                    onDateSelected = onDateSelected,
                    onDateReset = onDateReset,
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier.padding(end = padding.calculateEndPadding(LayoutDirection.Ltr)),
                    onClick = navigateToWrite
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.home_diary_icon),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            content = {
                padding = it
                when (diaries) {
                    is RequestState.Success -> {
                        HomeContent(
                            paddingValues = it,
                            diaryNotes = diaries.data,
                            onClick = navigateToWriteWithArgs
                        )
                    }

                    is RequestState.Error -> {
                        EmptyPage(
                            title = "Error", subtitle = "${diaries.error.message}"
                        )
                    }

                    is RequestState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    else -> {}
                }
            })
    }
}

@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Image(
                    modifier = Modifier
                        .size(250.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo Image",
                )
                NavigationDrawerItem(
                    label = {
                        Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.google_logo),
                                contentDescription = "Google Logo",
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign Out", color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    selected = false,
                    onClick = onSignOutClicked,
                )
                NavigationDrawerItem(
                    label = {
                        Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                            Image(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete All Icon",
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Delete All Diaries", color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    selected = false,
                    onClick = onDeleteAllClicked,
                )
            }
        },
        content = content,
    )
}