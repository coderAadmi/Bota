package com.poloman.bota.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.poloman.bota.R

@Composable
fun ProgressCard(
    modifier: Modifier,
    from : String, progress : Int,
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

            LinearProgressIndicator(
                progress = { progress.toFloat() / 100 },
                color = Color(0xFF0A80ED), trackColor = Color(0xFFE8EDF5),
                gapSize = 4.dp,
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