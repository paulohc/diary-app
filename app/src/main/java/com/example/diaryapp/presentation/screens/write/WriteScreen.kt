package com.example.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.diaryapp.model.Diary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WriteScreen(
    selectedDiary: Diary?,
    pagerState: PagerState,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit,
) {
    Scaffold(
        topBar = {
            WriteTopBar(
                selectedDiary = selectedDiary,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed,
            )
        },
        content = {
                  WriteContent(
                      pagerState = pagerState,
                      title = "",
                      onTitleChanged = {},
                      description = "",
                      onDescriptionChanged = {},
                      paddingValues = it,
                  )
        },
    )
}