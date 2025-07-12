package com.poloman.bota

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.poloman.bota.network.NetworkResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel

class QrViewModel @Inject constructor(private val repository: BotaRepository) : ViewModel() {
    fun getQrCodeState() : StateFlow<Drawable?> {
        return repository.qrCodeDrawable
    }

    fun generateQrCode(){
        repository.createQr()
    }

    fun getFilesByType(fileType : Int): Flow<PagingData<BotaFile>> {
        return repository.getFilesByType(fileType).cachedIn(viewModelScope)
    }

    fun getFilesCountByType(fileType: Int) : StateFlow<Int> {
        return repository.getFilesCountByType(viewModelScope,fileType)
    }

    fun getNetWorkActionState() : StateFlow<BotaRepository.NetworkAction>{
        return repository.networkAction
    }

    fun startBotaServer(){
        repository.startServer()
    }

    fun connectToClient(host : String){
        repository.connectToClient(host)
    }

    fun setNetworkServiceState(request: NetworkResponse) {
        repository.setNetworkServiceState(request)
    }

    fun getNetworkResponseState(): StateFlow<NetworkResponse> {
        return repository.networkResponseState
    }

    fun setSelectedFiles(uris: List<Uri>) {
        repository.setSelectedFilesState(uris)
    }

    fun getSelectedFiles(): StateFlow<List<Uri>> {
        return repository.getSelectedFilesState()
    }

    fun showUserSelector() {
        repository.showUserSelector()
    }

    fun hideUserSelector(){
        repository.hideUserSelector()
    }

    val userSelectorState = repository.userSelectorState
}