package com.example.syntax_camping.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.syntax_camping.dataClasses.Lottery

@Composable
fun LotteryListScreenOwner(
    lotteryList: List<Lottery>,
    onItemClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        lotteryList.forEach{ lottery ->
            LotteryCard(
                lottery = lottery,
                onClick = { onItemClick(lottery.lotteryId) }
            )
        }
    }
}