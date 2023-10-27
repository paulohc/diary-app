package com.example.auth

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.realm.kotlin.mongodb.*
import kotlinx.coroutines.*

data class AuthenticationUiState(
    val authenticated: Boolean = false,
    val loading: Boolean = false,
)

class AuthenticationViewModel : ViewModel() {

    var state by mutableStateOf(AuthenticationUiState())
        private set

    fun setLoading(loading: Boolean) {
        state = state.copy(loading = loading)
    }


    fun signInWithMongoAtlas(
        tokenId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    App.create(BuildConfig.APP_ID).login(
                        Credentials.jwt(tokenId)
                    ).loggedIn
                }
                withContext(Dispatchers.Main) {
                    if (result) {
                        onSuccess()
                        delay(600)
                        state = state.copy(authenticated = true)
                    } else {
                        onError(Exception("User is not logged in."))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }
}