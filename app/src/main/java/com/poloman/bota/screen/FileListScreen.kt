package com.poloman.bota.screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberImagePainter
import com.poloman.bota.BotaFile
import com.poloman.bota.FileType
import com.poloman.bota.QrViewModel
import java.io.File

@Composable
fun FileScreen(navController: NavController, contentType : Int){
    ConstraintLayout(modifier = Modifier
        .fillMaxSize()) {

        val (title, foldersList) = createRefs()
        val qrVm = hiltViewModel<QrViewModel>()
        val data = qrVm.getFilesByType(contentType).collectAsLazyPagingItems()

        Text(
            "Photos", fontWeight = FontWeight.Bold,
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

           items(data.itemCount) { index ->
               val file = data[index]
               file?.let {
                   FileCard2(it)
               }
           }
            data.apply {
                when {
                    loadState.append is LoadState.Loading -> {
                        item {

                        }
                    }

                    loadState.append is LoadState.Error -> {
                        item {
                            Text(text = "Error loading more items")
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun FileCard2(file : BotaFile,count : Int = 0) {
    val qrVm = hiltViewModel<QrViewModel>()
    Card(
        onClick = {}, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {

            val (icon, name, count) = createRefs()
            Image(
                painter = rememberImagePainter(Uri.fromFile(File(file.pathAndName))),
                contentDescription = "Folder",
                modifier = Modifier.constrainAs(icon) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, margin = 20.dp)
                    end.linkTo(parent.end, margin = 20.dp)
                    height = Dimension.value(200.dp)
                }
            )

            Text(text = file.fileName, modifier = Modifier.constrainAs(name) {
                top.linkTo(icon.bottom, margin = 8.dp)
                start.linkTo(parent.start, margin = 20.dp)
                end.linkTo(parent.end, margin = 20.dp)
                width = Dimension.fillToConstraints
            }, style = TextStyle(fontSize = 16.sp, color = Color.Black))


        }
    }
}