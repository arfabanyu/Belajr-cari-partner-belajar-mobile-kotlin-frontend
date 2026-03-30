import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belajr.controllers.FriendRepository
import com.example.belajr.controllers.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {

    private val matchRepo = MatchRepository()
    private val friendRepo = FriendRepository()

    private val _results = MutableStateFlow<List<PartnerWithStatus>>(emptyList())
    val results = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var searchJob: Job? = null

    fun search(keyword: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (keyword.isBlank()) {
                _results.value = emptyList()
                return@launch
            }
            delay(400) // tunggu 400ms setelah user berhenti ketik
            _isLoading.value = true
            matchRepo.searchPartners(keyword)
                .onSuccess { _results.value = it }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun sendRequest(receiverId: String, keyword: String) {
        viewModelScope.launch {
            friendRepo.sendRequest(receiverId)
                .onSuccess { search(keyword) } // refresh hasil search
                .onFailure { _error.value = it.message }
        }
    }

    fun acceptRequest(requestId: Long, senderId: String, keyword: String) {
        viewModelScope.launch {
            friendRepo.acceptRequest(requestId, senderId)
                .onSuccess { search(keyword) }
                .onFailure { _error.value = it.message }
        }
    }
}