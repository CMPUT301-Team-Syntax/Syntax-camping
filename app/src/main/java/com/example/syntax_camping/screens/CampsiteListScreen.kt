package com.example.syntax_camping.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.Campsite

@Composable
fun CampsiteListScreen(campsiteList: List<Campsite>, onItemClick: (String) -> Unit){
    Scaffold { innerPadding ->
        LazyColumn(Modifier
            .fillMaxSize()
            .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ){
            items(campsiteList.size){index ->
                CampsiteRowTab(campsiteList[index]) {
                    onItemClick(campsiteList[index].documentId)
                }
            }
        }
    }
}