package com.example.belajr.views

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.belajr.controllers.FriendRepository
import com.example.belajr.controllers.MatchRepository
import com.example.belajr.models.PartnerWithStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchViewModel : ViewModel() {

    private val matchRepo = MatchRepository()
    private val friendRepo = FriendRepository()

    private val _results = MutableStateFlow<List<PartnerWithStatus>>(emptyList())
    val results = _results.asStateFlow()

    // Untuk kompatibilitas dengan kode HomePage lama
    val partners: LiveData<List<PartnerWithStatus>> = _results.asLiveData()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun searchPartners(keyword: String) {
        viewModelScope.launch {
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
                .onSuccess { searchPartners(keyword) }
                .onFailure { _error.value = it.message }
        }
    }
}