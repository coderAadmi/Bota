package com.poloman.bota.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.poloman.bota.R

@Preview(showSystemUi = true)
@Composable
fun SettingsPage() {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF7FAFC))) {
        OptionCard("Connected Devices", painterResource(R.drawable.file)){}
        OptionCard("Help",painterResource(R.drawable.file)) { }
        OptionCard("FAQ", painterResource(R.drawable.file)) { }
        OptionCard("About", painterResource(R.drawable.file)) { }
    }
}


@Composable
fun OptionCard(title: String, painter : Painter, actionClick: () -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
            val (icon, text, next) = createRefs()
            Card(
                onClick = actionClick,
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EDF5)),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.constrainAs(icon) {
                    top.linkTo(parent.top, margin = 4.dp)
                    start.linkTo(parent.start, margin = 12.dp)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                }) {
                Icon(
                    painter = painter,
                    contentDescription = "Folder",
                    modifier = Modifier.padding( 8.dp )
                )
            }

            Text(text = title, modifier = Modifier.constrainAs(text) {
                top.linkTo(icon.top)
                bottom.linkTo(icon.bottom)
                start.linkTo(icon.end, margin = 8.dp)
                end.linkTo(next.start, margin = 8.dp)
                width = Dimension.fillToConstraints
            })

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Folder",
                modifier = Modifier.constrainAs(next) {
                    centerVerticallyTo(parent)
                    end.linkTo(parent.end, margin = 12.dp)
                })
        }
    }
}