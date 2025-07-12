package com.poloman.bota.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.poloman.bota.views.TransferPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(navController: NavController){
    Column(modifier = Modifier.fillMaxSize().padding(bottom = 1.dp)){
        CenterAlignedTopAppBar(title = {Text(text = "Files", fontWeight = FontWeight.Bold)},
            windowInsets = WindowInsets(0.dp),
            actions = {
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Go back")
                }
            }
        )
        TransferPage(navController)
    }
}