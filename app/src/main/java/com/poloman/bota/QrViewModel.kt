package com.poloman.bota

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.poloman.bota.network.NetworkResponse
import com.poloman.bota.network.TransferProgress
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

    fun setNetworkReqState(request: NetworkResponse) {
        repository.setNetworkRequestsState(request)
    }

    fun getNetworkReqState(): StateFlow<List<NetworkResponse>> {
        return repository.networkRequestsState
    }

    fun removeNetworkReqState(req : NetworkResponse){
        repository.removeRequest(req)
    }

    fun getPermissionDialogState(): StateFlow<Boolean> {
        return repository.isPermDialogShown
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

    fun setProgressState(ip : String, progress: TransferProgress){
        repository.setProgressMapState(ip, progress)
    }

    fun getProgressState(): StateFlow<Map<String, TransferProgress>> {
        return repository.getProgressMapState()
    }

    fun showProgressDialog(){
        repository.showProgressDialog()
    }

    fun hideProgressDialog(){
        repository.hideProgressDialog()
    }

    fun getProgressDialogState(): StateFlow<Boolean> {
        return repository.getProgressDialogState()
    }

    fun hidePermissionDialog() {
        repository.hidePermissionDialog()
    }

    fun showPermissionDialog() {
        repository.showPermissionDialog()
    }

    fun serverStopped() {
        repository.severStopped()
    }
}