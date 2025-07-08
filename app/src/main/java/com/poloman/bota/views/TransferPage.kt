package com.poloman.bota.views


import android.provider.CalendarContract.Colors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.poloman.bota.R

@Preview(showSystemUi = true)
@Composable
fun TransferPage() {
    ConstraintLayout {
        val (lv, btn) = createRefs()
        FoldersListView(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF7FAFC))
                .constrainAs(lv) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(btn.top, margin = 8.dp)
                    height = Dimension.fillToConstraints
                }
        )

        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .constrainAs(btn) {
                    centerHorizontallyTo(parent)
                    bottom.linkTo(parent.bottom)
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A80ED))) {
            Text(text = "Transfer")
        }
    }

}

@Composable
fun FoldersListView(modifier: Modifier) {
    ConstraintLayout(modifier = Modifier
        .fillMaxSize()) {
        val (title, foldersList) = createRefs()
        Text(
            "Folders", fontWeight = FontWeight.Bold,
            style = TextStyle(fontSize = 22.sp),
            modifier = Modifier.constrainAs(title) {
                top.linkTo(parent.top)
                start.linkTo(parent.start, margin = 12.dp)
            })
        LazyColumn(modifier = Modifier.constrainAs(foldersList) {
            top.linkTo(title.bottom, margin = 4.dp)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
        }) {

            items(5) {
                FileCard()
            }

        }
    }
}

@Composable
fun FileCard() {
    Card(
        onClick = {}, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {

            val (icon, name, count) = createRefs()
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EDF5)),
                modifier = Modifier.constrainAs(icon) {
                    top.linkTo(name.top)
                    start.linkTo(parent.start, margin = 8.dp)
                    bottom.linkTo(count.bottom)
                    width = Dimension.fillToConstraints
                }) {
                Icon(
                    painter = painterResource(R.drawable.folder),
                    contentDescription = "Folder",
                    modifier = Modifier.padding(6.dp)
                )
            }

            Text(text = "Directory", modifier = Modifier.constrainAs(name) {
                top.linkTo(parent.top, margin = 8.dp)
                start.linkTo(icon.end, margin = 20.dp)
                end.linkTo(parent.end, margin = 20.dp)
                width = Dimension.fillToConstraints
            }, style = TextStyle(fontSize = 16.sp, color = Color.Black))

            Text(text = "12 items", modifier = Modifier.constrainAs(count) {
                top.linkTo(name.bottom, margin = 2.dp)
                start.linkTo(icon.end, margin = 20.dp)
                end.linkTo(parent.end, margin = 20.dp)
                bottom.linkTo(parent.bottom, margin = 8.dp)
                width = Dimension.fillToConstraints
            }, style = TextStyle(color = Color(0xFF4A739C)))
        }
    }
}