package com.poloman.bota.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
import com.poloman.bota.network.BotaUser
import kotlinx.coroutines.flow.StateFlow


@Composable
fun UserSelectorDialog(connectedUsers: List<BotaUser>,
                         onDismiss : () -> Unit,
                         onSend :(users : List<BotaUser>) -> Unit){

    Dialog(onDismissRequest = onDismiss) {
        ConnectedUsers(connectedUsers, true) {
            selectedUsers ->
            onSend(selectedUsers)
        }
    }
}

@Composable
fun ConnectedUsersDialog(connectedUsers: List<BotaUser>, onDismiss: () -> Unit){

        Dialog(onDismissRequest = onDismiss) {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF7FAFC)).padding(4.dp)) {
                Text("Connected users", fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()){
                    items(connectedUsers) {
                        UserCard2(it.uname)
                    }
                }
            }
        }

}

@Composable
fun ConnectedUsers(connectedUsers: List<BotaUser>, isSelector : Boolean , onSend : (users : List<BotaUser>) -> Unit) {
    val selectedUsers = mutableListOf<BotaUser>()
    var btnColor by remember { mutableStateOf(Color(0xFFE8EDF5)) }
    var textColor by remember { mutableStateOf(Color.Black) }
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
                            if(selectedUsers.isNotEmpty()){
                                btnColor = Color(0xFF0A80ED)
                                textColor = Color.White
                            }
                            else{
                                btnColor = Color(0xFFE8EDF5)
                                textColor = Color.Black
                            }
                        }
                }
            }
                Button(
                    onClick = {
                        onSend(selectedUsers)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                        .constrainAs(sendBtn) {
                            centerHorizontallyTo(parent)
                            bottom.linkTo(parent.bottom, margin = 12.dp)
                        }) {
                    Text("Send", color = textColor)
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



@Composable
fun UserCard2(username : String){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (icon, text) = createRefs()
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
                end.linkTo(parent.end, margin = 12.dp)
                width = Dimension.fillToConstraints
            })

        }
    }
}