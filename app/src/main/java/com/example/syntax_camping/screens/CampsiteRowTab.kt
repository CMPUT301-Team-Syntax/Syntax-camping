package com.example.syntax_camping.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R


@Composable
fun CampsiteRowTab(campsite: Campsite, onClick:() -> Unit){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(33.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE2AFAF)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        )

    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically

        ){
            Box(
                contentAlignment = Alignment.CenterStart
            ){
                if (campsite.images.isNotEmpty()){
                    AsyncImage(
                        model = campsite.images[0],
                        contentDescription = " photo",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                }
                else{
                    Image(
                        painter = painterResource(id = R.drawable.home),
                        contentDescription = "No Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(8.dp)
                    )
                }

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Text(
                    text = campsite.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(text = campsite.location
                    , fontSize = 14.sp,
                    color = Color.Black,)

            }

        }



    }
}

