package com.poloman.bota.views


import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.poloman.bota.R
import com.poloman.bota.network.NetworkResponse
import com.poloman.bota.network.TransferProgress
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotaAppBar(
    destination: Int,
    progressState: StateFlow<Map<String, TransferProgress>>,
    progressDialogState: StateFlow<Boolean>,
    permissionDialogState : StateFlow<Boolean>,
    networkReqState : StateFlow<List<NetworkResponse>>,
    showDialog : ()-> Unit,
    showPermissionDialog : () -> Unit
) {
    var title = when(destination){
        0 -> "BoTA"
        1 -> "Files"
        2 -> "Settings"
        else -> ""
    }

        CenterAlignedTopAppBar(
            title = {
                Text(text = title, fontWeight = FontWeight.Bold)
            },
            windowInsets = WindowInsets(0.dp),
            actions = {
                val isDialogShown = progressDialogState.collectAsState().value
                if(progressState.collectAsState().value.isNotEmpty() && !isDialogShown) {

                    Card(
                        onClick = showDialog,
                        modifier = Modifier.padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EDF5))
                    ) {
                        Icon(
                            modifier = Modifier.padding(6.dp),
                            painter = painterResource(R.drawable.upload),
                            contentDescription = ""
                        )
                    }
                }

                if(!permissionDialogState.collectAsState().value && networkReqState.collectAsState().value.isNotEmpty()) {

                    Card(
                        onClick = showPermissionDialog,
                        modifier = Modifier.padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EDF5))
                    ) {
                        Icon(
                            modifier = Modifier.padding(6.dp),
                            painter = painterResource(R.drawable.file),
                            contentDescription = ""
                        )
                    }
                }
            }
        )
}