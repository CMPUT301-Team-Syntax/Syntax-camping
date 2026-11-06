package com.example.syntax_camping.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntax_camping.dataClasses.Lottery
import java.text.SimpleDateFormat
import androidx.compose.ui.platform.LocalLocale

@Composable
fun LotteryCard(lottery: Lottery,
                onClick: () -> Unit) {
    // Format the timestamp for display
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", LocalLocale.current.platformLocale)
    val drawDateStr = dateFormat.format(lottery.getReadableLotteryEndTime())
    val bookDateStr = dateFormat.format(lottery.getReadableBookingEndTime())

    // Calculate days left until the draw
    val daysLeft = ((lottery.endTimeLottery - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Lottery: ${lottery.name.takeIf { it.isNotBlank() } ?: "Pending ID"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Draws on: $drawDateStr",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Must book by: $bookDateStr",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )

                Text(
                    text = "${lottery.numWinners} winners • $daysLeft days left",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF3E6B68)
                )
            }
        }
    }
}