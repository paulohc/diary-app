package com.example.diaryapp.presentation.screens.write

import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.diaryapp.data.repository.MongoDB
import com.example.diaryapp.model.Mood
import com.example.diaryapp.navigation.ParameterIds
import com.example.diaryapp.util.RequestState
import kotlinx.coroutines.*
import org.mongodb.kbson.ObjectId

data class UiState(
    val selectedDiaryId: String? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
)

class WriteViewModel(
    private val savedStateHandle: SavedStateHandle,
): ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument() {
        uiState = uiState.copy(
            selectedDiaryId = savedStateHandle.get<String>(key = ParameterIds.DIARY_ID)
        )
    }

    private fun fetchSelectedDiary() {
        uiState.selectedDiaryId?.let { selectedDiaryId ->
            viewModelScope.launch(Dispatchers.Main) {
                val diary = MongoDB.getSelectedDiary(
                    diaryId = ObjectId.invoke(selectedDiaryId)
                )
                if (diary is RequestState.Success) {
                    withContext(Dispatchers.Main) {
                        setTitle(title = diary.data.title)
                        setDescription(description = diary.data.description)
                        setMood(mood = Mood.valueOf(diary.data.mood))
                    }
                }
            }
        }
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }
}