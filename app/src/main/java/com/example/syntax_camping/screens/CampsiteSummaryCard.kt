package com.example.syntax_camping.screens.userScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.navArgument
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.Rating

@Composable
fun CampsiteSummaryCard(campsite: Campsite) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* Navigate or Expand */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        )

        {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2994A)) // Orange placeholder
            )

            Spacer(modifier = Modifier.width(16.dp))


            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            )
            {

                Text(
                    text = campsite.getName(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.location_on),
                            contentDescription = "location",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Unspecified
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = campsite.getLocation(),
                            fontSize = 12.sp,
                            color = Color.Unspecified
                        )
                    }

                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.location_on),
                        contentDescription = "Capacity",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${campsite.getmaxOcc()} people max",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Text(
                    text = "304 people currently waiting",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                // Days Left
                Text(
                    text = "- days left",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Rating(campsite.getRating())
            }

        }
    }
}