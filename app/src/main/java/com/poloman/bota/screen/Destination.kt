package com.poloman.bota.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("songs", "Songs", Icons.Default.Home, "Home"),
    TRANSFER("album", "Album", Icons.Default.Send, "Transfer"),
    SETTINGS("playlist", "Playlist", Icons.Default.Settings, "Settings")
}