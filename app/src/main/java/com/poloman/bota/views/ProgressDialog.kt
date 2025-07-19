package com.poloman.bota.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProgressDialog(
    progressDialogState: StateFlow<Boolean>, progressMapState: StateFlow<Map<String, Int>>,
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
                            ProgressCard(Modifier, it.first, it.second, isReceiving = isReceiving)
                        }
                    }
                }
            }
        }
}