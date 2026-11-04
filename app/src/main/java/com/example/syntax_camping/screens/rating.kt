package com.example.syntax_camping.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.R

@Composable
fun Rating(numRating: Double) {
    Row(

    ) {

        val starIcons = remember(numRating) {
            var myInt = numRating.toInt()
            var decimalLeft = numRating - myInt
            val list = mutableListOf<Int>()

            repeat(5) {
                if (myInt >= 1) {
                    list.add(R.drawable.star)
                    myInt -= 1
                } else if (decimalLeft > 0.0) {
                    if (decimalLeft < 0.5) list.add(R.drawable.star_less)
                    else if (decimalLeft == 0.5) list.add(R.drawable.star_middle)
                    else list.add(R.drawable.star_more)
                    decimalLeft = 0.0
                } else {
                    list.add(R.drawable.star_empty)
                }
            }
            list
        }

        Row {
            starIcons.forEach { iconId ->
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = "star",
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}