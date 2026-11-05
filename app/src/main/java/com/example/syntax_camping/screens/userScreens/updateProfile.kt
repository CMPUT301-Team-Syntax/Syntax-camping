package com.example.syntax_camping.screens.userScreens

import android.R.attr.value
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp






@Composable
fun UpdateProfileScreen(modifier: Modifier = Modifier) {
    var nameValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }
    var phoneValue by remember { mutableStateOf("") }
    var locationValue by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp) // 👈 Spacing between fields
    ) {
        CustomUnderlineField(
            value = nameValue,
            onValueChange = { nameValue = it },
            placeholder = "Full Name"
        )

        CustomUnderlineField(
            value = emailValue,
            onValueChange = { emailValue = it },
            placeholder = "Email Address"
        )

        CustomUnderlineField(
            value = phoneValue,
            onValueChange = { phoneValue = it },
            placeholder = "Phone Number"
        )
        CustomUnderlineField(
            value = locationValue,
            onValueChange = { phoneValue = it },
            placeholder = "Location"
        )
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
    UpdateProfileScreen()
}