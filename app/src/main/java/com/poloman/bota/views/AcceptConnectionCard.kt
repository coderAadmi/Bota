package com.poloman.bota.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.poloman.bota.network.NetworkResponse
import kotlinx.coroutines.flow.StateFlow


@Composable
fun PermissionDialog(
    modifier: Modifier,
    isPermissionDialogShown: StateFlow<Boolean>,
    networkResponseState: StateFlow<List<NetworkResponse>>,
    onAccept: (ip: String, req: NetworkResponse) -> Unit,
    onDeny: (ip: String, req: NetworkResponse) -> Unit,
    onFileAccept: (ip: String, fname: String, size: Long, req: NetworkResponse) -> Unit,
    onMulFilesAccept: (ip: String, fCount: Int, size: Long, req: NetworkResponse) -> Unit,
    onFileDeny: (ip: String, req: NetworkResponse) -> Unit,
    onDismiss: () -> Unit
) {
    val response = networkResponseState.collectAsState().value

    if (isPermissionDialogShown.collectAsState().value && response.isNotEmpty())
        Dialog(onDismissRequest = onDismiss) {
            Card {
                LazyColumn {
                    items(response) {
                        when (it) {
                            is NetworkResponse.ConnectionRequest -> {
                                val from = it
                                AcceptConnectionCard(
                                    modifier = modifier.padding(horizontal = 12.dp),
                                    onAccept = { onAccept(from.ip, from) },
                                    onDeny = { onDeny(from.ip, from) },
                                    title = "Connection Request",
                                    information = "Accept incoming connection from ${from.name}"
                                )
                            }

                            is NetworkResponse.IncomingDataRequest -> {
                                val request = it
                                var sizeInKB = (request.size.toFloat()) / 1024
                                var sizeUnit = "KB"
                                var sizeInMB = 0f
                                var sizeShown = sizeInKB
                                if (sizeInKB > 1024) {
                                    sizeInMB = sizeInKB / 1024
                                    sizeUnit = "MB"
                                    sizeShown = sizeInMB
                                }
                                var sizeInGB = 0f
                                if (sizeInMB > 1024) {
                                    sizeInGB = sizeInMB / 1024
                                    sizeUnit = "GB"
                                    sizeShown = sizeInGB
                                }

                                AcceptConnectionCard(
                                    modifier = modifier,
                                    onAccept = {
                                        onFileAccept(
                                            request.ip,
                                            request.filename,
                                            request.size,
                                            request
                                        )
                                    },
                                    onDeny = { onFileDeny(request.ip, request) },
                                    title = "Incoming File",
                                    information = "Incoming file from ${request.name} of size $sizeShown $sizeUnit"
                                )
                            }

                            NetworkResponse.Nothing -> {
                                // Optionally show a Snackbar or Dialog here
                            }

                            is NetworkResponse.IncomingMulDataRequest -> {
                                val request = it
                                var sizeInKB = (request.size.toFloat()) / 1024
                                var sizeUnit = "KB"
                                var sizeInMB = 0f
                                var sizeShown = sizeInKB
                                if (sizeInKB > 1024) {
                                    sizeInMB = sizeInKB / 1024
                                    sizeUnit = "MB"
                                    sizeShown = sizeInMB
                                }
                                var sizeInGB = 0f
                                if (sizeInMB > 1024) {
                                    sizeInGB = sizeInMB / 1024
                                    sizeUnit = "GB"
                                    sizeShown = sizeInGB
                                }


                                AcceptConnectionCard(
                                    modifier = modifier,
                                    onAccept = {
                                        onMulFilesAccept(
                                            request.ip,
                                            request.fileCount,
                                            request.size,
                                            request
                                        )
                                    },
                                    onDeny = { onFileDeny(request.ip, request) },
                                    title = "Incoming Files",
                                    information = "Incoming ${request.fileCount} files from ${request.name} of size $sizeShown $sizeUnit"
                                )
                            }
                        }
                    }
                }
            }
        }
}

@Composable
fun AcceptConnectionCard(
    modifier: Modifier, onAccept: () -> Unit, onDeny: () -> Unit,
    title: String, information: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        ConstraintLayout(
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFFF7FAFC))
        ) {
            val (cardTitle, info, accept, deny) = createRefs()

            Text(text = title, modifier = Modifier.constrainAs(cardTitle) {
                top.linkTo(parent.top, margin = 4.dp)
                start.linkTo(parent.start, margin = 8.dp)
            }, fontWeight = FontWeight.Bold)

            Text(text = information, modifier = Modifier.constrainAs(info) {
                top.linkTo(cardTitle.bottom, margin = 4.dp)
                start.linkTo(parent.start, margin = 8.dp)
                end.linkTo(parent.end, margin = 8.dp)
                width = Dimension.fillToConstraints
            })


            Button(
                onClick = onDeny,
                modifier = Modifier.constrainAs(accept) {
                    top.linkTo(info.bottom, margin = 4.dp)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                    start.linkTo(parent.start, margin = 8.dp)
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
                    top.linkTo(info.bottom, margin = 4.dp)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                    end.linkTo(parent.end, margin = 8.dp)
                }) {
                Text("Accept")
            }

        }
    }
}