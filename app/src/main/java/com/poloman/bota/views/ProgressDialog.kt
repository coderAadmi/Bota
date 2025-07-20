package com.poloman.bota.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.poloman.bota.R
import com.poloman.bota.network.TransferProgress
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProgressDialog(
    progressDialogState: StateFlow<Boolean>, progressMapState: StateFlow<Map<String, TransferProgress>>,
    isReceiving: Boolean = true,
    onDismiss: () -> Unit
) {
    if (progressDialogState.collectAsState().value)
        Dialog(onDismissRequest = onDismiss) {

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC))
            ) {
                ConstraintLayout {
                    val (progressLv, title) = createRefs()

                    Text(
                        text = "Transfers", modifier = Modifier.constrainAs(title) {
                            top.linkTo(parent.top, margin = 12.dp)
                            start.linkTo(parent.start, margin = 20.dp)
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    val entries = progressMapState.collectAsState().value.toList()
                    LazyColumn(modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(progressLv) {
                            top.linkTo(title.bottom, margin = 8.dp)
                            bottom.linkTo(parent.bottom, margin = 12.dp)
                            centerHorizontallyTo(parent)
                        }) {
                        items(entries) {
                            when(it.second){

                                is TransferProgress.Transmitted -> {
                                    val response = it.second as TransferProgress.Transmitted
                                    ProgressCard(Modifier, response.uname, response.progress, isReceiving = it.first.startsWith("FROM"))
                                }

                                is TransferProgress.CalculatingSize -> {
                                    val response = it.second as TransferProgress.CalculatingSize
                                    StatusCard(Modifier, response.uname, "Calculating size ...", isReceiving = false)
                                }
                                is TransferProgress.Success -> {
                                    val response = it.second as TransferProgress.Success
                                    StatusCard(Modifier, response.uname, "Transmission Successful", isReceiving = response.isReceiving)
                                }
                                is TransferProgress.WaitingForPermissionToSend -> {
                                    val response = it.second as TransferProgress.WaitingForPermissionToSend
                                    StatusCard(Modifier, response.uname, "Waiting for permission...", isReceiving = false)
                                }
                                is TransferProgress.WaitingForSender -> {
                                    val response = it.second as TransferProgress.WaitingForSender
                                    StatusCard(Modifier, response.uname, "Waiting for sender...", isReceiving = true)
                                }
                                is TransferProgress.RequestDenied -> {
                                    val response = it.second as TransferProgress.RequestDenied
                                    StatusCard(Modifier, response.uname, "Permission denied...", isReceiving = false)
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
fun StatusCard(
    modifier: Modifier,
    from : String, status : String,
    isReceiving: Boolean = true
) {
    var icon = painterResource(R.drawable.download)
    if (!isReceiving)
        icon = painterResource(R.drawable.upload)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        modifier = modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (typeRef, unameRef, progressRef) = createRefs()


            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .constrainAs(typeRef) {
                        start.linkTo(parent.start, margin = 12.dp)
                        centerVerticallyTo(parent)
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EDF5))
            ) {
                Icon(
                    modifier = Modifier
                        .padding(8.dp),
                    painter = icon,
                    contentDescription = ""
                )
            }

            Text(text = from, modifier = Modifier.constrainAs(unameRef) {
                top.linkTo(parent.top, margin = 12.dp)
                start.linkTo(typeRef.end, margin = 12.dp)
            })

            Text( text = status,
                modifier = Modifier
                    .height(12.dp)
                    .constrainAs(progressRef) {
                        top.linkTo(unameRef.bottom, margin = 4.dp)
                        start.linkTo(unameRef.start)
                        bottom.linkTo(parent.bottom, margin = 12.dp)
                        end.linkTo(parent.end, margin = 12.dp)
                        width = Dimension.fillToConstraints
                    }
            )
        }
    }
}