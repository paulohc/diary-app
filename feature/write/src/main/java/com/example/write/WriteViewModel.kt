package com.example.write

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.*
import com.example.mongo.database.ImageToDeleteDao
import com.example.mongo.database.ImageToUploadDao
import com.example.mongo.database.entity.ImageToDelete
import com.example.mongo.database.entity.ImageToUpload
import com.example.mongo.repository.MongoDB
import com.example.ui.GalleryImage
import com.example.ui.GalleryState
import com.example.util.*
import com.example.util.model.Diary
import com.example.util.model.Mood
import com.example.util.model.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime
import javax.inject.Inject

data class UiState(
    val selectedDiaryId: String? = null,
    val selectedDiary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val updatedDateTime: RealmInstant? = null,
)

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imageToUploadDao: ImageToUploadDao,
    private val imageToDeleteDao: ImageToDeleteDao,
) : ViewModel() {

    val galleryState = GalleryState()
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
                MongoDB.getSelectedDiary(
                    diaryId = ObjectId.invoke(selectedDiaryId)
                ).catch { emit(RequestState.Error(Exception("Diary is already deleted."))) }
                    .collect { diary ->
                        if (diary is RequestState.Success) {
                            withContext(Dispatchers.Main) {
                                setSelectedDiary(diary = diary.data)
                                setTitle(title = diary.data.title)
                                setDescription(description = diary.data.description)
                                setMood(mood = Mood.valueOf(diary.data.mood))

                                fetchImagesFromFirebase(
                                    remoteImagePaths = diary.data.images,
                                    onImageDownload = { downloadedImage ->
                                        galleryState.addImage(
                                            GalleryImage(
                                                image = downloadedImage,
                                                remoteImagePath = extractImagePath(
                                                    fullImageUrl = downloadedImage.toString()
                                                )
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
            }
        }
    }

    private fun setSelectedDiary(diary: Diary) {
        uiState = uiState.copy(selectedDiary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    private fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    fun updateTime(zonedDateTime: ZonedDateTime) {
        uiState = uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant())
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedDiaryId != null) {
                updateDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            } else {
                insertDiary(diary = diary, onSuccess = onSuccess, onError = onError)
            }
        }
    }

    private suspend fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val result = MongoDB.insertNewDiary(diary = diary.apply {
            val updatedDateTime = uiState.updatedDateTime
            if (updatedDateTime != null) {
                date = updatedDateTime
            }
        })
        if (result is RequestState.Success) {
            uploadImagesToFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val selectedDiaryId = uiState.selectedDiaryId ?: return
        val result = MongoDB.updateDiary(diary = diary.apply {
            _id = ObjectId.invoke(selectedDiaryId)
            date = uiState.updatedDateTime ?: uiState.selectedDiary?.date ?: date
        })
        if (result is RequestState.Success) {
            uploadImagesToFirebase()
            deleteImagesFromFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val selectedDiaryId = uiState.selectedDiaryId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val result = MongoDB.deleteDiary(id = ObjectId.invoke(selectedDiaryId))
            if (result is RequestState.Success) {
                withContext(Dispatchers.Main) {
                    uiState.selectedDiary?.let { deleteImagesFromFirebase(images = it.images) }
                    onSuccess()
                }
            } else if (result is RequestState.Error) {
                withContext(Dispatchers.Main) {
                    onError(result.error.message.toString())
                }
            }
        }
    }

    fun addImage(image: Uri, imageType: String) {
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        Log.d("WriteViewModel", "remoteImagePath: $remoteImagePath")
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath,
            )
        )
    }

    private fun uploadImagesToFirebase() {
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri
                    if (sessionUri != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToUploadDao.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = galleryImage.remoteImagePath,
                                    imageUri = galleryImage.image.toString(),
                                    sessionUri = sessionUri.toString(),
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun deleteImagesFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        if (images != null) {
            images.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        } else {
            galleryState.imagesToBeDeleted.map { it.remoteImagePath }.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageToDeleteDao.addImageToDelete(
                                ImageToDelete(remoteImagePath = remotePath)
                            )
                        }
                    }
            }
        }
    }

    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }
}