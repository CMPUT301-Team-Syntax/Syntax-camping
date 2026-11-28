package com.example.syntax_camping.UserScreens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.syntax_camping.AppFirebaseMessagingService
import com.example.syntax_camping.R
import com.example.syntax_camping.UserViewModel
import com.example.syntax_camping.globalDeviceId
import com.example.syntax_camping.screens.tabs.AdminBottomBar
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.google.firebase.firestore.FirebaseFirestore

private val SettingsPink = Color(0xFFE2AEAF)
private val LinkBlue = Color(0xFF6093B5)

private fun hasSystemNotificationPermission(context: Context): Boolean {
    val runtimeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
    return runtimeGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
}

private fun openAppNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    context.startActivity(intent)
}

@Composable
fun SettingsScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    adminMode: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val deviceId = userViewModel.deviceId.ifBlank { globalDeviceId }

    var notificationsEnabled by remember {
        mutableStateOf(hasSystemNotificationPermission(context))
    }
    var rememberDevice by rememberSaveable { mutableStateOf(true) }
    var showAdminDialog by remember { mutableStateOf(false) }

    // Sync initial state from Firestore (in case user previously turned off notifications in-app)
    LaunchedEffect(deviceId) {
        if (deviceId.isNotBlank()) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(deviceId)
                .get()
                .addOnSuccessListener { doc ->
                    val tokens = doc.get("fcmTokens") as? List<*>
                    val hasToken = !tokens.isNullOrEmpty()
                    notificationsEnabled = hasSystemNotificationPermission(context) && hasToken
                }
        }
    }

    // Re-check OS notification permission when returning from Android system settings
    DisposableEffect(lifecycleOwner, deviceId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!hasSystemNotificationPermission(context)) {
                    notificationsEnabled = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Android 13+ runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            notificationsEnabled = true
            AppFirebaseMessagingService.createNotificationChannel(context)
            AppFirebaseMessagingService.syncCurrentToken(context, deviceId, forceOptIn = true)
        } else {
            notificationsEnabled = false
            openAppNotificationSettings(context)
        }
    }

    SettingsPagesLayout(navController, adminMode = adminMode) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp).padding(top = 28.dp, bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    .toggleable(
                        value = notificationsEnabled,
                        role = Role.Switch,
                        onValueChange = { enabled ->
                            if (enabled) {
                                if (hasSystemNotificationPermission(context)) {
                                    notificationsEnabled = true
                                    AppFirebaseMessagingService.createNotificationChannel(context)
                                    AppFirebaseMessagingService.syncCurrentToken(
                                        context,
                                        deviceId,
                                        forceOptIn = true
                                    )
                                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    openAppNotificationSettings(context)
                                }
                            } else {
                                notificationsEnabled = false
                                AppFirebaseMessagingService.disableNotificationsForDevice(
                                    context,
                                    deviceId
                                )
                            }
                        }
                    ),
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

            if (adminMode) {
                // back to the normal app: clears the admin screens and lands on Search
                SettingsMenuRow(
                    label = "Switch back to user mode",
                    modifier = Modifier.testTag("settingsSwitchToUserMode"),
                    onClick = {
                        navController.navigate("Search") {
                            popUpTo("Search") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                SettingsMenuRow(
                    label = "Login as admin",
                    modifier = Modifier.testTag("settingsLoginAsAdmin"),
                    onClick = { showAdminDialog = true }
                )
            }

            SettingsMenuRow(
                label = "View cancelled reservations",
                modifier = Modifier.testTag("settingsCancelledReservations"),
                onClick = { navController.navigate("cancelledReservations") }
            )
            TextButton(
                onClick = { navController.navigate(APP_GUIDELINES_ROUTE) { launchSingleTop = true } },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
            ) {
                Text("See app Policy and Guidelines", color = LinkBlue,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center)
            }
        }

        if (showAdminDialog) {
            AlertDialog(
                onDismissRequest = { showAdminDialog = false },
                modifier = Modifier.testTag("adminLoginDialog"),
                title = { Text("Login as admin?") },
                text = { Text("Do you want to continue to the admin screens?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAdminDialog = false
                            navController.navigate("adminBrowseCampsites") { launchSingleTop = true }
                        },
                        modifier = Modifier.testTag("adminLoginYes")
                    ) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showAdminDialog = false },
                        modifier = Modifier.testTag("adminLoginNo")
                    ) { Text("No") }
                }
            )
        }
    }
}

/** Other menu destinations are intentionally left for the team's later settings work. */
@Composable
private fun SettingsMenuRow(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
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
    adminMode: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        contentColor = Color.Black,
        topBar = { TopBar(navController) },
        bottomBar = {
            if (adminMode) {
                AdminBottomBar(navController, currentRoute = "adminSettings")
            } else {
                CustomBottomBar(navController, currentRoute = "Settings")
            }
        },
        content = content
    )
}