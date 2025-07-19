package com.poloman.bota.views


import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.poloman.bota.QrViewModel
import com.poloman.bota.R
import com.poloman.bota.network.Communicator
import kotlinx.coroutines.flow.StateFlow
import java.io.File

@Composable
fun TransferPage(communicator: Communicator) {

    val qrVm = hiltViewModel<QrViewModel>()
    val filePickerLauncher = rememberLauncherForActivityResult(object :
        ActivityResultContract<String, List<Uri>>() {
        override fun createIntent(
            context: Context,
            input: String
        ): Intent {
            return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = input //The input option es the MIME Type that you need to use
                putExtra(Intent.EXTRA_LOCAL_ONLY, true) //Return data on the local device
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) //If select one or more files
                    .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?
        ): List<Uri> {
            val result = mutableListOf<Uri>()
            if(resultCode == RESULT_OK){
                intent?.let {
                    it.clipData?.let { clipData->
                        for(i in 0 until clipData.itemCount){
                            result.add(clipData.getItemAt(i).uri)
                        }
                    }?: run{
                        it.data?.let { uri ->
                            result.add(uri)
                        }
                    }

                }
                return result
            }
            return  emptyList()
        }


    }) {
            result ->
        result.forEach { uri ->
            Log.d("BOTA_URIS",uri.path!!)
        }
        qrVm.setSelectedFiles(result)
    }

    ConstraintLayout(Modifier.fillMaxSize().background(Color(0xFFF7FAFC))) {
        val (label1, selectFilesBtn, sendBtn, selectedFilesView) = createRefs()

        Card (
            onClick = {
                filePickerLauncher.launch("*/*")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding( horizontal = 12.dp)
                .constrainAs(selectFilesBtn) {
                    centerHorizontallyTo(parent)
                    top.linkTo(parent.top, margin = 4.dp)
                }){
            ConstraintLayout(Modifier.fillMaxWidth()) {
                val (ic, tt) = createRefs()
                Icon(painter = painterResource(R.drawable.folder), contentDescription = "",
                    modifier = Modifier.constrainAs(ic) {
                        top.linkTo(parent.top, margin = 8.dp)
                        bottom.linkTo(parent.bottom,8.dp)
                        start.linkTo(parent.start, margin = 12.dp)
                    })
                Text(text = "Select files to transfer", modifier = Modifier.constrainAs(tt) {
                    centerVerticallyTo(parent)
                    start.linkTo(ic.end, margin  = 8.dp)
                    end.linkTo(parent.end, margin = 12.dp)
                })
            }
        }

        Text(text = "Selected Files",fontWeight = FontWeight.Bold,
            style = TextStyle(fontSize = 22.sp),
            modifier = Modifier.constrainAs(label1) {
                top.linkTo(selectFilesBtn.bottom, margin = 12.dp)
                start.linkTo(parent.start, margin = 12.dp)
            })

        FoldersListView(qrVm.getSelectedFiles(), Modifier.constrainAs(selectedFilesView) {
            top.linkTo(label1.bottom, margin = 12.dp)
            bottom.linkTo(sendBtn.top, margin = 12.dp)
            start.linkTo(parent.start, margin = 12.dp)
            end.linkTo(parent.end, margin = 12.dp)
            height = Dimension.fillToConstraints
        })



        Button(
            onClick = {
                communicator.showConnectedDevices()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .constrainAs(sendBtn) {
                    centerHorizontallyTo(parent)
                    bottom.linkTo(parent.bottom)
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A80ED))) {
            Text(text = "Transfer")
        }


    }

}

@Composable
fun FoldersListView(selectedFilesState : StateFlow<List<Uri>>, modifier: Modifier) {
    val uris = selectedFilesState.collectAsState().value
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(uris) { uri ->
            FileCard(Modifier, uri)
        }
    }

}

@Composable
fun FileCard(modifier : Modifier , uri : Uri) {
    val file = File(uri.path!!)
    Card(
        onClick = {

        },
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FAFC)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {

            val (icon, name, count) = createRefs()
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EDF5)),
                modifier = Modifier.constrainAs(icon) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start, margin = 8.dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }) {
                Icon(
                    painter = painterResource(R.drawable.file),
                    contentDescription = "Folder",
                    modifier = Modifier.padding(6.dp)
                )
            }

            Text(text = file.absolutePath, modifier = Modifier.constrainAs(name) {
                centerVerticallyTo(parent)
                start.linkTo(icon.end, margin = 20.dp)
                end.linkTo(parent.end, margin = 20.dp)
                width = Dimension.fillToConstraints
            }, style = TextStyle(fontSize = 16.sp, color = Color.Black))

        }
    }
}