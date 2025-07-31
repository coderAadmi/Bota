package com.poloman.bota

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.poloman.bota.network.Helper
import com.poloman.bota.network.NetworkResponse
import com.poloman.bota.network.TransferProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class BotaRepository @Inject constructor(private val appContext : Context,
    private val botaDb : BotaAppDb ) {

    private val _qrCodeDrawable = MutableStateFlow<Drawable?>(null)
    val qrCodeDrawable = _qrCodeDrawable.asStateFlow()

    fun createQr(){
        val data = QrData.Url(Helper.getdeviceIpAddress())
        val options = createQrVectorOptions {

            padding = .125f

            background {

            }

            logo {
                drawable = ContextCompat
                    .getDrawable(appContext, R.drawable.folder)
                size = .25f
                padding = QrVectorLogoPadding.Natural(.2f)
                shape = QrVectorLogoShape
                    .Circle
            }
            colors {
                dark = QrVectorColor
                    .Solid(ContextCompat.getColor(appContext, R.color.black))
            }
            shapes {

            }
        }

        _qrCodeDrawable.value = QrCodeDrawable(data,options)
    }


    fun getFilesByType(fileType : Int) : Flow<PagingData<BotaFile>> {
        val pagingSourceFactory = {botaDb.getBotaDao().getPagedFilesByCount(fileType)}
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    private val _isPermDialogShown = MutableStateFlow<Boolean>(false)
    val isPermDialogShown = _isPermDialogShown.asStateFlow()
    private val _networkRequestsState = MutableStateFlow<List<NetworkResponse>>(emptyList())
    val networkRequestsState = _networkRequestsState.asStateFlow()
    fun setNetworkRequestsState(request: NetworkResponse) {
        _networkRequestsState.update { oldList ->
            oldList + request
        }
        _isPermDialogShown.value = true
    }

    private val _networkResponseState = MutableStateFlow<NetworkResponse>(NetworkResponse.Nothing)
    val networkResponseState = _networkResponseState.asStateFlow()
    fun setNetworkServiceState(request: NetworkResponse) {
        _networkResponseState.value = request
    }

    private val _selectedFiles = MutableStateFlow<List<Uri>>(emptyList())
    val selectedFilesFlow = _selectedFiles.asStateFlow()
    fun setSelectedFilesState(uris: List<Uri>) {
        _selectedFiles.value = uris
    }

    fun getSelectedFilesState(): StateFlow<List<Uri>> {
        return selectedFilesFlow
    }

    private val _userSelectorState = MutableStateFlow<Boolean>(false)
    val userSelectorState = _userSelectorState.asStateFlow()
    fun showUserSelector() {
        _userSelectorState.value = true
    }

    fun hideUserSelector(){
        _userSelectorState.value = false
    }

    private val _progressState = MutableStateFlow<Map<String, TransferProgress>>(emptyMap())
    val progressState = _progressState.asStateFlow()

    fun setProgressMapState(ip : String, progress: TransferProgress){
        _progressState.update { oldMap ->
            oldMap + (ip to progress)
        }
        if(progress is TransferProgress.Success){
            CoroutineScope(Dispatchers.IO).launch {
                delay(2000)
                val map = _progressState.value.toMutableMap()
                map.remove(ip)
                _progressState.value = map
            }
        }
    }

    fun getProgressMapState(): StateFlow<Map<String, TransferProgress>> {
        return progressState
    }

    private val _progressDialogShown = MutableStateFlow<Boolean>(true)
    val progressDialogShown = _progressDialogShown.asStateFlow()

    fun showProgressDialog(){
        _progressDialogShown.value = true
    }

    fun hideProgressDialog(){
        _progressDialogShown.value = false
    }

    fun getProgressDialogState(): StateFlow<Boolean> {
        return progressDialogShown
    }

    fun removeRequest(response: NetworkResponse) {
        _networkRequestsState.update { oldList ->
            oldList - response
        }
    }

    fun hidePermissionDialog() {
        _isPermDialogShown.value = false
    }

    fun showPermissionDialog() {
        _isPermDialogShown.value = true
    }

    fun severStopped() {
        _qrCodeDrawable.value = null
    }

    fun removeUpdatesFor(ip: String) {
        val map = _progressState.value.toMutableMap()
        if(map.contains("FROM $ip")){
            map.remove("FROM $ip")
        }
        if(map.contains("TO $ip")){
            map.remove("TO $ip")
        }
        _progressState.value = map

        val list = _networkRequestsState.value.toMutableList()
        list.filter { nr ->
            if((nr is NetworkResponse.ConnectionRequest && nr.ip.equals(ip)) ||
                nr is NetworkResponse.IncomingMulDataRequest && nr.ip.equals(ip)){
                 false
            }
            true
        }
        _networkRequestsState.value = list
    }

}