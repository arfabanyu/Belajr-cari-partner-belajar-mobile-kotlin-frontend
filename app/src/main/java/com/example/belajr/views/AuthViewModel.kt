import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.belajr.controllers.AuthRepository
import com.example.belajr.controllers.NotificationRepository
import com.example.belajr.models.Profile
import com.example.belajr.models.ProfileUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile = _profile.asStateFlow()

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.register(email, password, username)
                .onSuccess { _authState.value = AuthState.Success }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Register gagal"
                    )
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.login(email, password)
                .onSuccess {
                    _authState.value = AuthState.Success
                    loadProfile()
                    // Register FCM token setelah login
                    NotificationRepository().registerFcmToken()
                }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Login gagal"
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            NotificationRepository().clearFcmToken()
            repo.logout()
                .onSuccess {
                    _authState.value = AuthState.Idle
                    _profile.value = null
                }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Logout gagal"
                    )
                }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            repo.getCurrentProfile()
                .onSuccess { _profile.value = it }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Gagal load profil"
                    )
                }
        }
    }

    fun updateProfile(data: ProfileUpdate) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repo.updateProfile(data)
                .onSuccess {
                    _authState.value = AuthState.Success
                    loadProfile()
                }
                .onFailure {
                    _authState.value = AuthState.Error(
                        it.message ?: "Gagal update profil"
                    )
                }
        }
    }

    fun checkSession() {
        if (repo.isLoggedIn()) loadProfile()
    }
}