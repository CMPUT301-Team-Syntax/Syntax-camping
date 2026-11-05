package com.example.syntax_camping.screens.userScreens

import android.R.attr.value
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun UpdateProfileScreen(navController: NavController, modifier: Modifier = Modifier) {
    var nameValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf("") }
    var locationValue by remember { mutableStateOf("") }





    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center, // 👈 Spacing between fields
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ,
            shape = RoundedCornerShape(33.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally // 👈 Centers circle + text!
            ){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF2994A)) // Orange placeholder


                ){}
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = " update Profile ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

                

        }
        Spacer(modifier = Modifier.height(30.dp))

        CustomUnderlineField(
            value = nameValue,
            onValueChange = { nameValue = it },
            placeholder = "Full Name"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomUnderlineField(
            value = emailValue,
            onValueChange = { emailValue = it },
            placeholder = "Email Address"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomUnderlineField(
            value = phoneValue,
            onValueChange = { phoneValue = it },
            placeholder = "Phone Number"
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomUnderlineField(
            value = locationValue,
            onValueChange = { locationValue = it },
            placeholder = "Location"
        )

        Spacer(modifier = Modifier.height(150.dp))

        Button(
            onClick = { /* Handle save button click */ },

            shape = RoundedCornerShape(33.dp),
            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 7.dp)
        ) {
            Text(text = "Save", fontWeight = FontWeight.Bold)
        }

    }
}


@Composable
fun CustomUnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = modifier.onFocusChanged { isFocused = it.isFocused },
        decorationBox = { innerTextField ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, color = Color.Gray)
                    }
                    innerTextField()
                }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(
                    color = if (isFocused) Color(0xFF3E6B68) else Color.LightGray,
                    thickness = if (isFocused) 2.dp else 1.dp
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun UpdateProfilePreview() {
    UpdateProfileScreen(
        navController = TODO(),
        modifier = TODO()
    )
}