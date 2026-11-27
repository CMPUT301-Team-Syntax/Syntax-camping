package com.example.syntax_camping.screens.userScreens.reservationScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.example.syntax_camping.ui.components.PromptCsvDownload

/**
 * Shows a reservation CSV as text, with a Download button.
 * details = extra lines shown above the CSV (label to value).
 */
@Composable
fun CsvViewerScreen(
    navController: NavController,
    title: String,
    csv: String?,
    loaded: Boolean,
    fileName: String,
    details: List<Pair<String, String>> = emptyList(),
    notFoundText: String = "Nothing to show"
) {
    var csvToDownload by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("csvViewerScreen"),
        containerColor = Color.White,
        topBar = { TopBar(navController) },
        bottomBar = {
            CustomBottomBar(
                navController = navController,
                currentRoute = "Reservations",
                backgroundColor = Color(0xFF3E6B68)
            )
        }
    ) { innerPadding ->
        when {
            !loaded -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(modifier = Modifier.testTag("csvViewerLoading")) }

            csv == null || csv.isBlank() -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { Text(notFoundText, modifier = Modifier.testTag("csvViewerNotFound")) }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("csvViewerTitle")
                )

                details.forEach { (label, value) ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(label, fontSize = 12.sp, color = Color.Gray)
                        Text(value, fontSize = 16.sp, color = Color.Black)
                    }
                }

                Text("CSV", fontSize = 12.sp, color = Color.Gray)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF4E8D5), RoundedCornerShape(12.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = csv,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color.Black,
                        modifier = Modifier.testTag("csvViewerContent")
                    )
                }

                Button(
                    onClick = { csvToDownload = csv },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("csvViewerDownload"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
                ) {
                    Text("Download CSV", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    PromptCsvDownload(
        csvContent = csvToDownload,
        defaultFileName = fileName,
        onDismiss = { csvToDownload = null }
    )
}