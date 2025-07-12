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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class BotaRepository @Inject constructor(private val appContext : Context,
    private val botaDb : BotaAppDb) {

    sealed class NetworkAction{
        object START_SERVER : NetworkAction()
        data class  CONNECT_TO_CLIENT(val host : String) : NetworkAction()
        object NOT_STARTED : NetworkAction()
        object STARTED : NetworkAction()
    }

    private val _qrCodeDrawable = MutableStateFlow<Drawable?>(null)
    val qrCodeDrawable = _qrCodeDrawable.asStateFlow()

    private val _nsEvent = MutableStateFlow<NetworkAction>(NetworkAction.NOT_STARTED)
    val networkAction : StateFlow<NetworkAction> = _nsEvent.asStateFlow()


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

    fun startServer() {
        _nsEvent.value = NetworkAction.START_SERVER
    }

    fun connectToClient(hostAddress: String) {
        _nsEvent.value = NetworkAction.CONNECT_TO_CLIENT(hostAddress)
    }

    fun getFilesByType(fileType : Int) : Flow<PagingData<BotaFile>> {
        val pagingSourceFactory = {botaDb.getBotaDao().getPagedFilesByCount(fileType)}
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = pagingSourceFactory
        ).flow
    }

    fun getFilesCountByType(viewModelScope: CoroutineScope, fileType : Int) : StateFlow<Int> =
        botaDb.getBotaDao().getFileTypeCount(fileType).stateIn(viewModelScope, SharingStarted.Eagerly, 0)


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


}