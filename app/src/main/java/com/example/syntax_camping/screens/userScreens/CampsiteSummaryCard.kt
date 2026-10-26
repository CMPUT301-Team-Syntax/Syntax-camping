package com.example.syntax_camping.screens.userScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.Rating

@Composable
fun CampsiteSummaryCard(campsite: Campsite){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )

        {
            Column(
                modifier = Modifier.weight(1f)
            )
                {
                    campsite.getPhotos()
                        Image(
                            painter = painterResource(id = R.drawable.missing_image_filler),
                            contentDescription = "A description of the image for accessibility",
                            modifier = Modifier.size(300.dp)
                        )

                    Rating(campsite.getRating())
                }

                Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1.2f) // Takes up the right half of the card
            )
                {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ){
                        Text(
                            text = campsite.getName(),
                            fontSize = 16.sp
                        )
                    }



            // 1. Title Text ("Dolor Campsite")
            // 2. Location Row (Pin Icon + "Edmonton")
            // 3. Info Text ("9 people max")
            // 4. Waiting Text ("304 people currently waiting")
            // 5. Days Left Text ("5 days left")
            // 6. Clickable Text ("Apply now")
        }
    }

}