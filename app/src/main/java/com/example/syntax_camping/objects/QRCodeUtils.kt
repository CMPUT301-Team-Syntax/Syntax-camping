package com.example.syntax_camping.objects

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

//AUTHOR: Sorachat Chaichana
//Date: 2026-11-19
//Object class specifically just for QR code generation.

object QRCodeUtils {
    fun generateCampsiteQRCode(campsiteId: String): Bitmap {
        val size = 512  //Max resolution of qr code (512 by 512 pixels as of now.)

        val content = "syntaxcamping:campsite:$campsiteId"

        //Creates QR code that represents itself as a matrix.
        val matrix = QRCodeWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            size,
            size
        )

        //Creates a bitmap.
        val bitmap = createBitmap(size, size, Bitmap.Config.ARGB_8888)

        //Generates the actual visual representation of the QR code.
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (matrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        return bitmap   //Returns generated QR code as a bitmap from campsiteId.
    }
}