import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belajr.controllers.FriendRepository
import com.example.belajr.controllers.MessageRepository
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.decodeRecord
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {

    private val repo = MessageRepository()
    private val friendRepo = FriendRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms = _chatRooms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var activeChannel: RealtimeChannel? = null

    fun openChat(otherUserId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            repo.getMessages(otherUserId)
                .onSuccess { _messages.value = it }
                .onFailure { _error.value = it.message }

            val channel = repo.getChannel(otherUserId)
            activeChannel = channel

            channel.postgresChangeFlow<PostgresAction.Insert>(
                schema = "public"
            ) {
                table = "messages"
            }.onEach { change: PostgresAction.Insert ->
                val newMessage: Message = change.decodeRecord()
                val isRelevant =
                    newMessage.senderId == otherUserId ||
                            newMessage.receiverId == otherUserId
                if (isRelevant) {
                    _messages.value = _messages.value + newMessage
                }
            }.launchIn(viewModelScope)

            repo.subscribeChannel(channel)
            _isLoading.value = false
        }
    }

    fun closeChat() {
        viewModelScope.launch {
            activeChannel?.let { repo.unsubscribeChannel(it) }
            activeChannel = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        closeChat()
    }

    fun sendMessage(receiverId: String, content: String) {
        viewModelScope.launch {
            repo.sendMessage(receiverId, content)
                .onFailure { _error.value = it.message }
        }
    }

    fun sendWithAttachment(
        receiverId: String,
        content: String? = null,
        attachmentUrl: String
    ) {
        viewModelScope.launch {
            repo.sendMessageWithAttachment(receiverId, content, attachmentUrl)
                .onFailure { _error.value = it.message }
        }
    }

    fun loadChatRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            friendRepo.getFriends()
                .onSuccess { friends ->
                    repo.getChatRooms(friends)
                        .onSuccess { _chatRooms.value = it }
                        .onFailure { _error.value = it.message }
                }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun closeChat(otherUserId: String) {
        viewModelScope.launch {
            repo.unlistenMessages(otherUserId)
        }
    }
}