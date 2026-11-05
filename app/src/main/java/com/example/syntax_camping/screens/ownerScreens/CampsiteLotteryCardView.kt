package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.R
import androidx.compose.ui.text.font.FontWeight
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.screens.LotteryListScreenOwner


@Composable
fun CampsiteLotteryCardView(
    lotteries: List<Lottery>,
    onAddLotteryClick: () -> Unit,
    onLotteryClick: (String) -> Unit,
    modifier: Modifier = Modifier
    ) {
        Column(modifier = modifier
            .fillMaxWidth()) {
            Text(
                text = "Your Lotteries",
                modifier = Modifier
                    .padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )

            if (lotteries.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No running Lotteries",
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )
                    IconButton(
                        onClick = onAddLotteryClick,
                        modifier = Modifier
                            .size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.add),
                            contentDescription = "add Lottery",
                            tint = Color.Unspecified
                        )
                    }
                }
            } else {
                Box {
                    LotteryListScreenOwner(
                        lotteryList = lotteries,
                        onItemClick = onLotteryClick
                    )
                }
            }
        }
    }