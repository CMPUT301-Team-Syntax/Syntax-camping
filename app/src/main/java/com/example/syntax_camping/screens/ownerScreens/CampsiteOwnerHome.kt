package com.example.syntax_camping.screens.ownerScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.syntax_camping.Campsite
import com.example.syntax_camping.CampsiteRepository
import com.example.syntax_camping.R
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.lotteryRepository
import com.example.syntax_camping.screens.ownerScreens.ownerCampsiteList.CampsiteListScreenOwner
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.example.syntax_camping.screens.filterOwnedCampsites
import com.example.syntax_camping.screens.ownerScreens.lotteryScreens.lotteryList.CampsiteLotteryCardView

@Composable
fun CampsiteOwnerHome(
    navController: NavController,
    userViewModel: UserViewModel,
    onEditCampsite: (Campsite?) -> Unit,
    onEditLottery: (Lottery?) -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var noLotteryAvailable by remember { mutableStateOf(false) }
    var lotteriesOwned = remember { mutableStateListOf<Lottery>() }
    var ownerCampsites by remember { mutableStateOf<List<Campsite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    //  Trigger a database refresh every time this screen appears
    LaunchedEffect(Unit) {
        isLoading = true
        CampsiteRepository.refreshCampsiteCache { success ->
            if (success) {
                ownerCampsites = CampsiteRepository.getCampsites().toList()
            }
            isLoading = false
        }
    }

    val myCampsites = filterOwnedCampsites(ownerCampsites, userViewModel.currentUser?.ownerData)

    var filteredCampsiteList = myCampsites.filter { campsite ->
        campsite.lotteryId.isNullOrBlank()
    }
    val myLotteryIds = myCampsites
        .mapNotNull { campsite -> campsite.lotteryId?.takeIf { it.isNotBlank() } }
        .distinct()

    LaunchedEffect(myLotteryIds) {
        if (myLotteryIds.isEmpty()) {
            lotteriesOwned.clear()
        } else {
            lotteryRepository().getLotteries { allLotteries ->
                lotteriesOwned.clear()
                lotteriesOwned.addAll(allLotteries.filter { it.lotteryId in myLotteryIds })
            }
        }
    }


    val scrollState = rememberScrollState()





    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Set the background color to white
            .testTag("ownerHomeScreen"),
        containerColor = Color.White,
        topBar = { // top bar with back button and notification icon
            TopBar(
                navController
            )
        },
        bottomBar = {
            CustomBottomBar(navController, "CampsiteOwnerHome")
        }
    ) { innerPadding ->

        Column( modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(modifier = Modifier.weight(1f)) {
                if (isLoading|| userViewModel.currentUser == null) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag("ownerLoading"))
                } else if (myCampsites.isNotEmpty()) {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth()
                                .background(Color.White),

                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Your Campsites Info",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Box(contentAlignment = Alignment.TopEnd) {
                                IconButton(
                                    onClick = { isMenuExpanded = true },
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(44.dp)
                                        .testTag("ownerThreeDotsMenu")
                                    ,

                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.more_vertical),
                                        contentDescription = "add info",
                                        tint = Color.Unspecified
                                    )
                                }

                                DropdownMenu(
                                    expanded = isMenuExpanded,
                                    onDismissRequest = { isMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Add Campsite") },
                                        modifier = Modifier.testTag("addCampsiteMenuItem"),
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.location_on),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            isMenuExpanded = false
                                            onEditCampsite(null)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Add Lottery") },
                                        modifier = Modifier.testTag("addLotteryMenuItem"),

                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.lottery),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },

                                        onClick = {
                                            if (filteredCampsiteList.isNotEmpty()) {
                                                isMenuExpanded = false
                                                onEditLottery(null)
                                            } else {
                                                isMenuExpanded = false
                                                noLotteryAvailable = true
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Your Lotteries",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold

                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(scrollState),
                        ) {

                            CampsiteLotteryCardView(
                                lotteries = lotteriesOwned,
                                onAddLotteryClick = { onEditLottery(null) },
                                onLotteryClick = { clickedId ->
                                    navController.navigate("LotteryDetailsScreen/$clickedId")
                                },
                                modifier = Modifier.testTag("OwnerLotterySection")
                            )

                        Text(
                            text = "Your Campsites",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold

                        )

                        Box (modifier = Modifier.testTag("ownerCampsiteList")){
                            CampsiteListScreenOwner(
                                campsiteList = myCampsites,
                                onItemClick = { clickedId ->
                                    navController.navigate("ownerCampsiteDetails/$clickedId")
                                },
                                onUpdateClick = { clickedId ->
                                    val clickedCamp =
                                        ownerCampsites.find { it.documentId == clickedId }
                                    onEditCampsite(clickedCamp)
                                },
                                onViewLotClick = { lotteryId ->
                                    navController.navigate("LotteryDetailsScreen/$lotteryId")
                                }
                            )
                        }
                        }
                    }
                }else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.spacedBy(15.dp)
                    ) {
                        Text("Add new Campsite")

                        IconButton(onClick = { onEditCampsite(null) },
                            modifier = Modifier.
                            size(40.dp)
                                .testTag("addCampsiteButton")

                        )


                        {
                            Icon(
                                painter = painterResource(id = R.drawable.add),
                                contentDescription = "add campsite",
                                tint = Color.Unspecified,
                                modifier = Modifier.testTag("addCampsite")
                                )
                        }
                    }
                    Text(
                        "No campsites found.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (noLotteryAvailable) {
            AlertDialog(
                onDismissRequest = { noLotteryAvailable = false },
                modifier = Modifier.testTag("noLotteryDialog"),
                title = {
                    Text(text = "No Lottery Slot Available")
                },
                text = {
                    Text(text = "All your campsites are already in lotteries.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { noLotteryAvailable = false },
                        modifier = Modifier.testTag("noLotteryDialogOk")
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}