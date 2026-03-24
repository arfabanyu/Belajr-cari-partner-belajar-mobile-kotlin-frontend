import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belajr.controllers.AttachmentRepository
import com.example.belajr.controllers.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Success(val url: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

class AttachmentViewModel(private val context: Context) : ViewModel() {

    private val repo = AttachmentRepository(context)
    private val messageRepo = MessageRepository()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState = _uploadState.asStateFlow()

    // Upload file lalu langsung kirim sebagai pesan
    fun uploadAndSend(
        uri: Uri,
        receiverId: String,
        caption: String? = null
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading

            repo.uploadFile(uri)
                .onSuccess { url ->
                    _uploadState.value = UploadState.Success(url)

                    // Langsung kirim pesan dengan attachment
                    messageRepo.sendMessageWithAttachment(
                        receiverId = receiverId,
                        content = caption,
                        attachmentUrl = url
                    ).onFailure {
                        _uploadState.value = UploadState.Error(
                            it.message ?: "Gagal kirim pesan"
                        )
                    }
                }
                .onFailure {
                    _uploadState.value = UploadState.Error(
                        it.message ?: "Gagal upload file"
                    )
                }
        }
    }

    // Reset state setelah upload selesai
    fun resetState() {
        _uploadState.value = UploadState.Idle
    }
}