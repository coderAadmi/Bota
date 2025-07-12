package com.poloman.bota.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.poloman.bota.QrViewModel
import com.poloman.bota.network.NetworkResponse
import com.poloman.bota.network.NetworkService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow


@Composable
fun PermissionDialog(modifier: Modifier,
                     networkResponseState : StateFlow<NetworkResponse>,
                     onAccept : (ip : String) -> Unit, onDeny: (ip : String) -> Unit,
                     onFileAccept : (ip : String, fname : String, size : Long) -> Unit,
                     onFileDeny : (ip : String) -> Unit
) {
    val response = networkResponseState.collectAsState()

    when (response.value) {
        is NetworkResponse.ConnectionRequest -> {
            val from = response.value as NetworkResponse.ConnectionRequest
            Dialog(onDismissRequest = { onDeny(from.ip) }) {
                AcceptConnectionCard(
                    modifier = modifier.padding(horizontal = 12.dp),
                    onAccept = {onAccept(from.ip)},
                    onDeny = {onDeny(from.ip)},
                    title = "Connection Request",
                    information = "Accept incoming connection from ${from.name}"
                )
            }
        }

        is NetworkResponse.IncomingDataRequest -> {
            val request = response.value as NetworkResponse.IncomingDataRequest
            var sizeInKB = (request.size.toFloat()) /1024
            var sizeUnit = "KB"
            var sizeInMB = 0f
            var sizeShown = sizeInKB
            if(sizeInKB > 1024){
                sizeInMB = sizeInKB/1024
                sizeUnit = "MB"
                sizeShown = sizeInMB
            }
            var sizeInGB = 0f
            if(sizeInMB > 1024){
                sizeInGB = sizeInMB/1024
                sizeUnit = "GB"
                sizeShown = sizeInGB
            }

            Dialog(onDismissRequest = { onFileDeny(request.ip) }) {
                AcceptConnectionCard(
                    modifier = modifier,
                    onAccept = { onFileAccept(request.ip, request.filename, request.size ) },
                    onDeny = { onFileDeny(request.ip) },
                    title = "Incoming File",
                    information = "Incoming file from ${request.name} of size $sizeShown $sizeUnit"
                )
            }
        }

        NetworkResponse.Nothing -> {
            // Optionally show a Snackbar or Dialog here
        }
    }
}

@Composable
fun AcceptConnectionCard(modifier: Modifier, onAccept: () -> Unit, onDeny: () -> Unit,
                         title : String, information : String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ConstraintLayout(modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF7FAFC))) {
            val (cardTitle, info, accept, deny) = createRefs()

            Text(text = title, modifier = Modifier.constrainAs(cardTitle) {
                top.linkTo(parent.top, margin = 24.dp)
                start.linkTo(parent.start, margin = 12.dp)
            }, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Text(text = information, modifier = Modifier.constrainAs(info) {
                top.linkTo(cardTitle.bottom, margin = 16.dp)
                start.linkTo(parent.start, margin = 12.dp)
                end.linkTo(parent.end, margin = 12.dp)
                width = Dimension.fillToConstraints
            }, fontSize = 16.sp)


            Button(
                onClick = onDeny,
                modifier = Modifier.constrainAs(accept) {
                    top.linkTo(info.bottom, margin = 24.dp)
                    bottom.linkTo(parent.bottom, margin = 12.dp)
                    start.linkTo(parent.start, margin = 12.dp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ButtonDefaults.buttonColors().disabledContainerColor)
            ) {
                Text("Deny", color = Color.Black)
            }

            Button(
                onClick = onAccept,
                colors = ButtonDefaults
                    .buttonColors(containerColor = Color(0xFF0A80ED)),
                modifier = Modifier.constrainAs(deny) {
                    top.linkTo(info.bottom, margin = 24.dp)
                    bottom.linkTo(parent.bottom, margin = 12.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                }) {
                Text("Accept")
            }

        }
    }
}