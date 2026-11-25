//package com.example.syntax_camping.screens.userScreens
//
//import androidx.compose.foundation.layout.*
//
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//
//import kotlinx.coroutines.launch
//@Composable
//fun FilterSideBar(){
//    ModalDrawerSheet {
//        var selectedItem by remember { mutableStateOf("Home") }
//
//        NavigationDrawerItem(
//            // Call your existing icon composable right here
//            icon = { YourCustomIconComposable() },
//            label = { Text("Home") },
//            selected = selectedItem == "Home",
//            onClick = {
//                selectedItem = "Home"
//                scope.launch { drawerState.close() }
//            },
//            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
//        )
//    }
//
//}
