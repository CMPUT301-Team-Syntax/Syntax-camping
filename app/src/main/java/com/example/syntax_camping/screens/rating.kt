package com.example.syntax_camping.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.syntax_camping.R

@Composable
fun Rating(num: Float){
    Row(

    ){

        val myInt = num.toInt()
        val dec = num - myInt
        repeat(myInt) { index ->
            Icon(
                painter = painterResource(id = R.drawable.star),
                contentDescription = "star",
                tint = Color.Unspecified
            )
        }


        if (myInt < 5){
            if (dec.toDouble() == 0.0){
                Icon(
                    painter = painterResource(id = R.drawable.star_empty),
                    contentDescription = "star",
                    tint = Color.Unspecified
                )
            }

            else if (dec < 0.5){
                Icon(
                    painter = painterResource(id = R.drawable.star_less),
                    contentDescription = "star",
                    tint = Color.Unspecified
                )
            }

            else if (dec.toDouble() == 0.5){
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

    }
}