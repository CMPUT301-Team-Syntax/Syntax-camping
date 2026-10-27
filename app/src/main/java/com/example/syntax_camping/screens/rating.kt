package com.example.syntax_camping.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.syntax_camping.R

@Composable
fun Rating(numRating: Float){
    Row(

    ){

        var myInt = numRating.toInt()
        val decimalLeft = numRating - myInt
        repeat(5) { index ->
            if (myInt >= 1){
                Icon(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = "star",
                    tint = Color.Unspecified
                )
                myInt -= 1
            }
            
            else if (decimalLeft.toDouble() > 0.0){
                if (decimalLeft < 0.5){
                    Icon(
                        painter = painterResource(id = R.drawable.star_less),
                        contentDescription = "star",
                        tint = Color.Unspecified
                    )
                }

                else if (decimalLeft == 0.5f){
                    Icon(
                        painter = painterResource(id = R.drawable.star_middle),
                        contentDescription = "star",
                        tint = Color.Unspecified
                    )
                }

                else {
                    Icon(
                        painter = painterResource(id = R.drawable.star_more),
                        contentDescription = "star",
                        tint = Color.Unspecified
                    )
                }
            }

            else {
                Icon(
                    painter = painterResource(id = R.drawable.star_empty),
                    contentDescription = "star",
                    tint = Color.Unspecified
                )
            }
        }
    }
}