package com.example.auth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.stevdzasan.messagebar.*
import com.stevdzasan.onetap.*

@Composable
fun AuthenticationScreen(
    state: AuthenticationUiState,
    oneTapState: OneTapSignInState,
    messageBarState: MessageBarState,
    onButtonClicked: () -> Unit,
    onSuccessfulFirebaseSignIn: (String) -> Unit,
    onFailedFirebaseSignIn: (Exception) -> Unit,
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
            val credential = GoogleAuthProvider.getCredential(tokenId, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onSuccessfulFirebaseSignIn(tokenId)
                    } else {
                        task.exception?.let { onFailedFirebaseSignIn(it) }
                    }
                }
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