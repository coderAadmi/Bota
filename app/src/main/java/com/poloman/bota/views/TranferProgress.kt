package com.poloman.bota.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.flow.StateFlow


@Composable
fun ProgressCard(modifier : Modifier, progressMapState : StateFlow<Map<String, Int>>) {

    progressMapState.collectAsState().value.forEach {
        Dialog(onDismissRequest = {

        }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
                modifier = modifier
                .padding(4.dp)
                .fillMaxWidth()) {
                ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
                    val (typeRef, unameRef, progressRef) = createRefs()

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(4.dp)
                            .constrainAs(typeRef) {
                                start.linkTo(parent.start, margin = 12.dp)
                                centerVerticallyTo(parent)
                            })

                    Text(text = it.key, modifier = Modifier.constrainAs(unameRef) {
                        top.linkTo(parent.top, margin = 12.dp)
                        start.linkTo(typeRef.end, margin = 12.dp)
                    })

                    LinearProgressIndicator(
                        progress = { it.value.toFloat()/100 },
                        color = Color(0xFF0A80ED), trackColor = Color(0xFFE8EDF5),
                        modifier = Modifier
                            .height(4.dp)
                            .constrainAs(progressRef) {
                                top.linkTo(unameRef.bottom, margin = 4.dp)
                                start.linkTo(typeRef.end, margin = 12.dp)
                                bottom.linkTo(parent.bottom, margin = 12.dp)
                                end.linkTo(parent.end, margin = 12.dp)
                            }
                    )
                }
            }
        }
    }

}