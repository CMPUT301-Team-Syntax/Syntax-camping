package com.example.syntax_camping.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode


//Author:Sorachat Chaichana
//Date: 2026-11-19
//Composable function to scan a QR code and brings you to the campsite screen.
//Returns a true/false callback based if it works or not.

@Composable
fun CampsiteQRScanner(onCampsiteScanned: (String) -> Unit) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    //Button to display QR code scanner.
    Button(
        onClick = {
            errorMessage = null

            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()

            val scanner = GmsBarcodeScanning.getClient(context, options)

            scanner.startScan()  //Scans for the campsiteId, removing its prefix.
                .addOnSuccessListener { barcode ->
                    val content = barcode.rawValue.orEmpty()   //Either exists or not
                    val prefix = "syntaxcamping:campsite:"

                    if (content.startsWith(prefix)) {
                        val campsiteId = content.removePrefix((prefix))

                        if (campsiteId.isNotBlank() && !campsiteId.contains("/")) {
                            onCampsiteScanned(campsiteId)
                        } else {
                            errorMessage = "Invalid campsite ID!"
                        }
                    }
                    else {
                        errorMessage = "Not a Syntax Camping QR code!"
                    }
                }
                .addOnFailureListener { errorMessage = "Unable to scan QR code." }
        }
    ) {
        Text("Scan QR Code")
    }

    errorMessage?.let {Text(it)}
}