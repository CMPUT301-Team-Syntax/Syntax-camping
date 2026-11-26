package com.example.syntax_camping.screens.adminScreens.adminUserView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.screens.adminScreens.adminUserList.AdminUserItem
import com.example.syntax_camping.screens.adminScreens.adminUserList.UserInfoCard

@Composable
fun UserListScreen(
    users: List<AdminUserItem>,
    onItemClick: (String) -> Unit   // userId -> go to adminUserInfo
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(users, key = { it.id }) { u ->
            UserInfoCard(u) { onItemClick(u.id) }
        }
    }
}