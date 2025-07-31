package com.poloman.bota.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.flow.StateFlow

@Composable
fun InputDialog(isDialogShown : StateFlow<Boolean>, saveUserName : (String) -> Unit, cancel : () -> Unit){
    if(isDialogShown.collectAsState().value)
    Dialog(onDismissRequest = {
        cancel()
    }) {
        InputCard(onSave = { name ->
            saveUserName(name)
        }){
            cancel()
        }
    }
}

@Composable
fun InputCard(onSave : (String) -> Unit, onCancel : () -> Unit){
    var name by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth()) {
        ConstraintLayout {
            val (titleRef, nameRef, okRef, cancelRef) = createRefs()

            Text(text = "Enter your name", modifier = Modifier.constrainAs(titleRef) {
                top.linkTo(parent.top, margin = 12.dp)
                start.linkTo(parent.start, margin = 12.dp)
            })

            TextField(value = name,
                onValueChange = { newValue ->
                    name = newValue
                }, modifier = Modifier.constrainAs(nameRef) {
                    top.linkTo(titleRef.bottom, margin = 8.dp)
                    start.linkTo(parent.start, margin = 12.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                    width = Dimension.fillToConstraints
                })

            Button(onClick = {
                onSave(name)
            }, modifier = Modifier.constrainAs(okRef) {
                top.linkTo(nameRef.bottom, margin = 12.dp)
                bottom.linkTo(parent.bottom, margin = 12.dp)
                end.linkTo(parent.end, margin = 12.dp)
            }) {
                Text("Save")
            }

            Button(onClick = onCancel, modifier = Modifier.constrainAs(okRef) {
                top.linkTo(nameRef.bottom, margin = 12.dp)
                bottom.linkTo(parent.bottom, margin = 12.dp)
                start.linkTo(parent.start, margin = 12.dp)
            }) {
                Text("Cancel")
            }
        }
    }
}