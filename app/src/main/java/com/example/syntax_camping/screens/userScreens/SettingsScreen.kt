package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.TopBar
import com.example.syntax_camping.screens.CustomBottomBar

private val SettingsPink = Color(0xFFE2AEAF)
private val LinkBlue = Color(0xFF6093B5)

/** Storyboard shell. These local control states do not change notification delivery or sign-in. */
@Composable
fun SettingsScreen(navController: NavController) {
    var notificationsEnabled by rememberSaveable { mutableStateOf(false) }
    var rememberDevice by rememberSaveable { mutableStateOf(true) }

    SettingsPagesLayout(navController) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 28.dp, bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .toggleable(value = notificationsEnabled, role = Role.Switch,
                        onValueChange = { notificationsEnabled = it }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable notifications", modifier = Modifier.weight(1f),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = notificationsEnabled, onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = SettingsPink, checkedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCCCCCF),
                        uncheckedThumbColor = Color.White,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(Modifier.height(30.dp))
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .toggleable(value = rememberDevice, role = Role.Checkbox,
                        onValueChange = { rememberDevice = it }),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(checked = rememberDevice, onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF454545)))
                Text("Remember my device so I don’t have to sign in again",
                    fontSize = 13.sp, lineHeight = 19.sp)
            }
            Spacer(Modifier.height(44.dp))
            listOf("Data and Storage", "Help and Feedback", "View FAQ",
                "Report a Problem", "About Syntax Camping").forEach { label ->
                SettingsMenuRow(label)
            }
            TextButton(
                onClick = { navController.navigate(APP_GUIDELINES_ROUTE) { launchSingleTop = true } },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
            ) {
                Text("See app Policy and Guidelines", color = LinkBlue,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

/** Other menu destinations are intentionally left for the team's later settings work. */
@Composable
private fun SettingsMenuRow(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(18.dp).border(1.5.dp, Color.Gray, CircleShape),
            contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.star_empty), null,
                modifier = Modifier.size(12.dp), tint = Color.Gray)
        }
        Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold, color = Color(0xFF454545))
        Icon(painterResource(R.drawable.forward), null,
            modifier = Modifier.size(8.dp, 12.dp), tint = Color.Gray)
    }
}

/** Reuses the team's app bars on both Settings and Policy pages. */
@Composable
internal fun SettingsPagesLayout(
    navController: NavController,
    backgroundColor: Color = Color.White,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        contentColor = Color.Black,
        topBar = { TopBar(navController) },
        bottomBar = { CustomBottomBar(navController, currentRoute = "Settings") },
        content = content
    )
}

@Preview(showBackground = true, widthDp = 411, heightDp = 890)
@Composable
private fun SettingsPreview() { SettingsScreen(rememberNavController()) }
