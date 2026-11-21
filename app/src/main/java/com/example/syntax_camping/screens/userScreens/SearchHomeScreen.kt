package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.userScreens.userCampsiteList.CampsiteListScreen
import com.example.syntax_camping.screens.tabs.TopBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.testTag
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SearchHomeScreen(navController: NavController, userViewModel: UserViewModel){

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var  SearchInput by remember { mutableStateOf("")  }
    var FlitteredCampsiteList by remember { mutableStateOf(listOf<Campsite>()) }

    var realCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var displayFillereted by remember { mutableStateOf(false) }
    var StartDateRange by remember { mutableStateOf("") }
    var EndDateRange by remember { mutableStateOf("") }
    var numGuests by remember { mutableStateOf(1) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text("Location") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )
                NavigationDrawerItem(
                    label = { Text("Number  of Guests") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } }
                )

                Row() {
                    TextField(
                        value = StartDateRange,
                        onValueChange = { newValue -> StartDateRange = newValue },
                        label = { Text("Start Date") },
                        modifier = Modifier
                            .width(150.dp)
                            .padding(16.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    TextField(
                        value = EndDateRange,
                        onValueChange = { newValue -> EndDateRange = newValue },
                        label = { Text("End Date") },
                        modifier = Modifier
                            .width(150.dp)
                            .padding(16.dp)
                    )
                }


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center

                ) {

                    IconButton(onClick =  {numGuests ++}){
                        Icon(
                            painter = painterResource(id = R.drawable.ic_plus),
                            contentDescription = "add",
                            tint = Color.Unspecified
                        )
                    }
                    Spacer(modifier = Modifier.width(40.dp))

                    Text(text = numGuests.toString())

                    Spacer(modifier = Modifier.width(40.dp))


                    IconButton(onClick =  {if(numGuests > 1) numGuests--  }){
                        Icon(
                            painter = painterResource(id = R.drawable.ic_minus),
                            contentDescription = "add",
                            tint = Color.Unspecified
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))


                }

                Row( modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center){
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Apply Fliter")
                    }
                }

            }

        }
    ) {
        Scaffold(

            modifier = Modifier
                .fillMaxSize()
                .background(Color.White), // Set the background color to white
            containerColor = Color.White,
            topBar = {
                TopBar(navController)
            },

            bottomBar = {
                CustomBottomBar(
                    navController = navController,
                    currentRoute = "Search",
                    backgroundColor = Color(0xFF3E6B68)
                )
            }
        ) { innerPadding ->

            //inside the scaffold, this would be the content of the screen, start editing screen content from in here

            Column(
                modifier = Modifier

                    .padding(innerPadding)
                    .padding(9.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(){

                    TextField(
                        value = SearchInput,
                        onValueChange = { newValue -> SearchInput = newValue },
                        label = { Text("Type in the location or Campsite Name") },

                        shape = RoundedCornerShape(30.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,   // Removes line when typing
                            unfocusedIndicatorColor = Color.Transparent, // Removes line when idle
                            disabledIndicatorColor = Color.Transparent   // Removes line if disabled
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("search_input_field")
                            .padding(bottom = 16.dp)
                    )


                    Spacer(modifier = Modifier.width(8.dp))

                    LaunchedEffect(Unit) {
                        CampsiteRepository.refreshCampsiteCache { success ->
                            if (success) {
                                realCampsites = CampsiteRepository.getCampsites().toList()
                            }
                        }
                    }



                    IconButton(onClick = {
                        val listToFilter = if (realCampsites.isNotEmpty()) realCampsites else CampsiteRepository.getCampsites()
                        FlitteredCampsiteList = listToFilter.filter { campsite ->
                            campsite.name.contains(SearchInput, ignoreCase = true) || campsite.location.contains(SearchInput, ignoreCase = true)
                        }
                        displayFillereted = true
                    },
                        modifier = Modifier
                            .background(Color(0xFF3E6B68), shape = RoundedCornerShape(50))
                            .testTag("search_icon")

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),

                            contentDescription = "Search",
                            tint = Color.Unspecified
                        )
                    }
                    IconButton(onClick = { scope.launch { drawerState.open() } })
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Filter",
                            tint = Color.Unspecified
                        )
                    }

                }


//realCampsites is the actual firebase list of campsites.
                if (displayFillereted) {
                    Box(modifier = Modifier.weight(1f)) {

                        if (FlitteredCampsiteList.isEmpty()) {//database info
                            // Show this if the database is empty or failed to load
                            Text("No campsites found in database.")
                        } else {
                            // Show the actual list of cards!
                            CampsiteListScreen(
                                campsiteList = FlitteredCampsiteList,
                                onItemClick = { clickedId ->
                                    navController.navigate("campsiteDetails/$clickedId") //navigates to the campsite details information for the card clicked
                                }
                            )
                        }

                    }
                }
                else{
                    Box(modifier = Modifier.weight(1f)) {

                        if (realCampsites.isEmpty()) {//database info
                            // Show this if the database is empty or failed to load
                            Text("No campsites found in database.")
                        } else {
                            // Show the actual list of cards!
                            CampsiteListScreen(
                                campsiteList = realCampsites,
                                onItemClick = { clickedId ->
                                    navController.navigate("campsiteDetails/$clickedId") //navigates to the campsite details information for the card clicked
                                }
                            )
                        }

                    }
                }

            }
        }
    }
}


/// hello