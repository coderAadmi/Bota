package com.poloman.bota.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.poloman.bota.BotaUser



@Composable
fun ConnectedUsersDialog(connectedUsers: List<BotaUser>,  onDismiss : () -> Unit, onSend :(users : List<BotaUser>) -> Unit){

    Dialog(onDismissRequest = onDismiss) {
        ConnectedUsers(connectedUsers){
            selectedUsers ->
            onSend(selectedUsers)
        }
    }
}

@Composable
fun ConnectedUsers(connectedUsers: List<BotaUser>, onSend : (users : List<BotaUser>) -> Unit) {
    val selectedUsers = mutableListOf<BotaUser>()
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC))) {
        ConstraintLayout {
            val (usersListView, title, sendBtn) = createRefs()

            Text(text = "Connected Users", modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top, margin = 12.dp)
                start.linkTo(parent.start, margin = 20.dp)
            },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold)

            LazyColumn(modifier = Modifier.fillMaxWidth().constrainAs(usersListView) {
                top.linkTo(title.bottom, margin = 8.dp)
                bottom.linkTo(sendBtn.top, margin = 12.dp)
                centerHorizontallyTo(parent)
            }) {
                items(connectedUsers){
                    UserCardView(it.uname){
                        isSelected : Boolean ->
                        when(isSelected){
                            true -> selectedUsers.add(it)
                            false -> selectedUsers.remove(it)
                        }
                    }
                }
            }

            Button(onClick = {
                onSend(selectedUsers)
            }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).constrainAs(sendBtn) {
                centerHorizontallyTo(parent)
                bottom.linkTo(parent.bottom, margin = 12.dp)
            }) {
                Text("Send")
            }
        }
    }
}

@Composable
fun UserCardView(username : String, onSelectChange : (isSelected : Boolean) -> Unit){
    var isChecked by remember { mutableStateOf<Boolean>(false) }
    Card(
        onClick = {
            isChecked = !isChecked
            onSelectChange(isChecked)
        },
        modifier = Modifier
        .fillMaxWidth()
        .padding(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (icon, text, next) = createRefs()
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EDF5)),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.constrainAs(icon) {
                    top.linkTo(parent.top, margin = 4.dp)
                    start.linkTo(parent.start, margin = 12.dp)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                }) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "User Icon",
                    modifier = Modifier.padding( 8.dp )
                )
            }

            Text(text = username, modifier = Modifier.constrainAs(text) {
                top.linkTo(icon.top)
                bottom.linkTo(icon.bottom)
                start.linkTo(icon.end, margin = 12.dp)
                end.linkTo(next.start, margin = 12.dp)
                width = Dimension.fillToConstraints
            })

            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = !isChecked
                    onSelectChange(isChecked)
                },
                modifier = Modifier.constrainAs(next) {
                    centerVerticallyTo(parent)
                    end.linkTo(parent.end, margin = 12.dp)
                })
        }
    }
}