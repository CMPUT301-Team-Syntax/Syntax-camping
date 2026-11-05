package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.R

@Composable
fun CampsiteSelectorDropdown(
    myOwnedCampsites: List<Campsite>,
    selectedCampsites: MutableList<Campsite>
) {
    var expanded by remember { mutableStateOf(false) }

    // Only campsites that are NOT already in a lottery, and NOT currently selected in the UI
    val availableCampsites = myOwnedCampsites.filter { campsite ->
        campsite.lotteryId == null && !selectedCampsites.contains(campsite)
    }

    // Outer container styled to look like an OutlinedTextField
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.Gray,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Campsites in Lottery",
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        // 1. Display currently selected campsites with a remove button
        // (Removed the isNotEmpty check - if empty, the forEach just skips)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedCampsites.forEach { campsite ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = campsite.name)

                    // The "-" button to remove the campsite
                    IconButton(
                        onClick = {
                            selectedCampsites.remove(campsite)
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.remove_button),
                            contentDescription = "Remove",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 2. The button/row to add MORE campsites
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (availableCampsites.isNotEmpty()) {
                            expanded = !expanded
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (availableCampsites.isEmpty()) {
                    Text(
                        text = "No campsites available for lottery...",
                        color = Color.DarkGray
                    )
                } else {
                    Text(
                        text = "Add a campsite...",
                        color = Color.DarkGray
                    )

                    val iconSelected = if (expanded) R.drawable.material_symbol_down else R.drawable.material_symbol

                    Icon(
                        painter = painterResource(id = iconSelected),
                        contentDescription = "Expand Dropdown",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 3. Dropdown Menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                availableCampsites.forEach { campsite ->
                    DropdownMenuItem(
                        text = { Text(text = campsite.name) },
                        onClick = {
                            selectedCampsites.add(campsite)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}