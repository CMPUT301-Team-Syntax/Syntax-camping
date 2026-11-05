package com.example.syntax_camping.screens.userScreens

import android.R.attr.value
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.onFocusChanged
import java.lang.reflect.Modifier

@Composable

fun updateProfile(modifier: Modifier){
    var isFocused by remember { mutableStateOf(false) }
    val onValueChange = null
    BasicTextField(
        value = "hey ",
        onValueChange = { },
        singleLine = true,
        modifier = modifier .
        onFocusChanged { isFocused = it.isFocused },
    )
    ){



    }

}