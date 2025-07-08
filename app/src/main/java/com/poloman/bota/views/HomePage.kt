package com.poloman.bota.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Preview(showSystemUi = true)
@Composable
fun HomePage() {
    ConstraintLayout(modifier = Modifier
        .fillMaxSize().padding(bottom = 1.dp).background(Color(0xFFF7FAFC))) {
        val (title, strategy, genQR, scanQR) = createRefs()

        Text("Bota",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.constrainAs(title) {
            top.linkTo(parent.top, margin = 24.dp)
            centerHorizontallyTo(parent)
        })

        Text(
            "Choose how you want to connect to other devices on your local network",
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .constrainAs(strategy) {
                    top.linkTo(title.bottom, margin = 12.dp)
                    centerHorizontallyTo(parent)
                })

        Button(
            colors = ButtonDefaults
                .buttonColors(containerColor = Color(0xFF0A80ED)),
            onClick = {}, modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .constrainAs(genQR) {
                    top.linkTo(strategy.bottom, margin = 16.dp)
                    centerHorizontallyTo(parent)
                }) {
            Text("Generate QR",fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = {}, colors = ButtonDefaults
                .buttonColors(containerColor = ButtonDefaults.buttonColors().disabledContainerColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .constrainAs(scanQR) {
                    top.linkTo(genQR.bottom, margin = 4.dp)
                    centerHorizontallyTo(parent)
                }) {
            Text("Scan QR Code", style = TextStyle(color = Color.Black), fontWeight = FontWeight.Bold)
        }


    }
}