package com.example.syntax_camping.screens.adminScreens.adminCampsiteList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.Campsite

@Composable
fun CampsiteListScreenAdmin(
    campsiteList: List<Campsite>,
    onItemClick: (String) -> Unit,
    onViewLogsClick: ((String) -> Unit)? = null,
    onViewOwnerProfileClick: ((String) -> Unit)? = null,
    onViewNotificationsClick: ((String) -> Unit)? = null
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(campsiteList, key = { it.documentId }) { camp ->
            CampsiteCardAdmin(
                campsite = camp,
                onClick = { onItemClick(camp.documentId) },
                onViewLogsClick = onViewLogsClick?.let { cb -> { cb(camp.documentId) } },
                onViewOwnerProfileClick = onViewOwnerProfileClick?.let { cb -> { cb(camp.documentId) } },
                onViewNotificationsClick = onViewNotificationsClick?.let { cb -> { cb(camp.documentId) } }
            )
        }
    }
}
