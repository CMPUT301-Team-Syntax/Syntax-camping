package com.example.syntax_camping.ui.components // Adjust to match your package

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun PromptCsvDownload(
    csvContent: String?,
    defaultFileName: String,
    onDismiss: () -> Unit
) {
    // Only compose the launcher and dialog when there is actual CSV content to download
    if (csvContent == null) return

    val context = LocalContext.current

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                Toast.makeText(context, "CSV saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save CSV", Toast.LENGTH_SHORT).show()
            }
        }
        // Reset the state so the dialog closes after saving or cancelling the file picker
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download Reservation CSV") },
        text = { Text("Would you like to save a CSV copy of your reservation details?") },
        confirmButton = {
            TextButton(
                onClick = { createCsvLauncher.launch(defaultFileName) }
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No Thanks")
            }
        }
    )
}
/*
//CODE TO INCLUDE IN SCREENS WHERE SCSV DOWNLOAD OCCURS
//at the top with other remembered vars
var csvToDownload by remember { mutableStateOf<String?>(null) }
var csvFileName by remember { mutableStateOf("reservation.csv") }
//Wherever you wnat the pop up to appear, so probably right after reservation creation/cancellation
PromptCsvDownload(
        csvContent = csvToDownload,
        defaultFileName = csvFileName,
        onDismiss = { csvToDownload = null }
    )


//THis is a kind of updated call, not it is just so you can see how stuff is returned kind of, as you will still need to keep whatever old code was in th ecallback already as well
reservation.createReservation { success, csv ->
    if (success) {
        csvFileName = "reservation_${reservation.reservationId}.csv"
        csvToDownload = csv
    }
}

 */