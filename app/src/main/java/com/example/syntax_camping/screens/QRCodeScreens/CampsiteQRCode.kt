package com.example.syntax_camping.screens.QRCodeScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.objects.QRCodeUtils

//AUTHOR: Sorachat Chaichana
//Date: 2026-11-19
//A composable class to display the actual QR code itself.

@Composable
fun CampsiteQRCode(campsiteId: String) {

    val qrBitmap = remember(campsiteId) {
        QRCodeUtils.generateCampsiteQRCode(campsiteId)
    }

    Image(
        bitmap = qrBitmap.asImageBitmap(),
        contentDescription = "Campsite QR Code",
        modifier = Modifier.size(200.dp)
    )
}