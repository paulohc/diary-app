package com.example.diaryapp.presentation.screens.auth

import android.annotation.*
import android.util.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.example.diaryapp.*
import com.example.diaryapp.BuildConfig
import com.stevdzasan.messagebar.*
import com.stevdzasan.onetap.*

@Composable
fun AuthenticationScreen(
    state: AuthenticationUiState,
    oneTapState: OneTapSignInState,
    messageBarState: MessageBarState,
    onButtonClicked: () -> Unit,
    onTokenIdReceived: (String) -> Unit,
    onDialogDismissed: (String) -> Unit,
    navigateToHome: () -> Unit,
) {
    Scaffold { paddings ->
        ContentWithMessageBar(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddings),
            messageBarState = messageBarState
        ) {
            AuthenticationContent(
                loadingState = state.loading,
                onButtonClicked = onButtonClicked,
            )
        }
    }

    OneTapSignInWithGoogle(
        state = oneTapState,
        clientId = BuildConfig.CLIENT_ID,
        onTokenIdReceived = { tokenId ->
            onTokenIdReceived(tokenId)
        },
        onDialogDismissed = { message ->
            onDialogDismissed(message)
        }
    )

    LaunchedEffect(state.authenticated) {
        if (state.authenticated) {
            navigateToHome()
        }
    }
}