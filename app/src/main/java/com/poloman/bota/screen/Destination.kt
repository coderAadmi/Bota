package com.poloman.bota.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("songs", "Home", Icons.Default.Home),
    TRANSFER("album", "Transfer", Icons.Default.Send),
    SETTINGS("playlist", "More", Icons.Default.Settings)
}