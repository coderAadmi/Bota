package com.poloman.bota.views

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.poloman.bota.QrViewModel
import com.poloman.bota.network.Communicator
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode


@Composable
fun HomePage(communicator: Communicator) {
    val qrVm = hiltViewModel<QrViewModel>()
    val qrCodeResult = rememberLauncherForActivityResult(ScanQRCode()) { result : QRResult->
        when(result){
            is QRResult.QRError -> {

            }
            QRResult.QRMissingPermission -> {

            }
            is QRResult.QRSuccess -> {
                Log.d("BOTA_QR", "QR Result $result")
                communicator.onConnectToServer(result.content.rawValue!!)
            }
            QRResult.QRUserCanceled -> {

            }
        }
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                qrCodeResult.launch(null)
            } else {

            }
        }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 1.dp)
            .background(Color(0xFFF7FAFC))
    ) {
        val ( strategy, genQR, scanQR, qrImg) = createRefs()
        val context = LocalContext.current


        Text(
            "Choose how you want to connect to other devices on your local network",
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .constrainAs(strategy) {
                    top.linkTo(parent.top, margin = 12.dp)
                    centerHorizontallyTo(parent)
                })

        Button(
            onClick = {
                communicator.onStartServer()
            },
            colors = ButtonDefaults
                .buttonColors(containerColor = Color(0xFF0A80ED)), modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .constrainAs(genQR) {
                    top.linkTo(strategy.bottom, margin = 16.dp)
                    centerHorizontallyTo(parent)
                }) {

            Text("Generate QR", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = {
                if (ContextCompat
                        .checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                ) {
                    qrCodeResult.launch(null)
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }

            }, colors = ButtonDefaults
                .buttonColors(containerColor = ButtonDefaults.buttonColors().disabledContainerColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .constrainAs(scanQR) {
                    top.linkTo(genQR.bottom, margin = 4.dp)
                    centerHorizontallyTo(parent)
                }) {
            Text(
                "Scan QR Code",
                style = TextStyle(color = Color.Black),
                fontWeight = FontWeight.Bold
            )
        }

        qrVm.getQrCodeState().collectAsState().value?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "",
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .constrainAs(qrImg) {
                        top.linkTo(scanQR.bottom, margin = 80.dp)
                        centerHorizontallyTo(parent)
                        bottom.linkTo(parent.bottom, margin = 80.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints

                    })
        }

    }
}