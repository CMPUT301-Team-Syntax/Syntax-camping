package com.example.syntax_camping.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.R

@Composable
fun Rating(numRating: Double){
    Row(

    ){

        var myInt = numRating.toInt()
        var decimalLeft = numRating - myInt
        repeat(5) { index ->
            if (myInt >= 1){
                Icon(
                    painter = painterResource(id = R.drawable.star),
                    contentDescription = "star",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
                myInt -= 1
            }
            
            else if (decimalLeft> 0.0){
                if (decimalLeft < 0.5){
                    Icon(
                        painter = painterResource(id = R.drawable.star_less),
                        contentDescription = "star",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                }

                else if (decimalLeft == 0.5){
                    Icon(
                        painter = painterResource(id = R.drawable.star_middle),
                        contentDescription = "star",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                }

                else {
                    Icon(
                        painter = painterResource(id = R.drawable.star_more),
                        contentDescription = "star",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified
                    )
                }

                decimalLeft = 0.0
            }

            else {
                Icon(
                    painter = painterResource(id = R.drawable.star_empty),
                    contentDescription = "star",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}