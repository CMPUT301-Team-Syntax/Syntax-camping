package com.example.syntax_camping.UserScreens

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.syntax_camping.R
import com.example.syntax_camping.screens.tabs.CustomBottomBar
import com.example.syntax_camping.screens.tabs.TopBar
import com.example.syntax_camping.screens.CampsiteQRCode

private const val CAMPSITE_RULES_ROUTE = "CampsiteRules"
private const val CANCELLATION_POLICY_ROUTE = "CancellationPolicy"

private val rulesSections = listOf(
    R.string.campsite_rules_check_in_title to R.string.campsite_rules_check_in_body,
    R.string.campsite_rules_quiet_hours_title to R.string.campsite_rules_quiet_hours_body,
    R.string.campsite_rules_campfires_title to R.string.campsite_rules_campfires_body
)

private val cancellationSections = listOf(
    R.string.cancellation_policy_early_title to R.string.cancellation_policy_early_body,
    R.string.cancellation_policy_late_title to R.string.cancellation_policy_late_body,
    R.string.cancellation_policy_no_show_title to R.string.cancellation_policy_no_show_body
)

internal fun NavGraphBuilder.campsiteInformationDestinations(navController: NavController) {
    composable(CAMPSITE_RULES_ROUTE) {
        CampsiteInformationScreen(navController, CAMPSITE_RULES_ROUTE, R.string.campsite_rules_title, rulesSections)
    }
    composable(CANCELLATION_POLICY_ROUTE) {
        CampsiteInformationScreen(
            navController,
            CANCELLATION_POLICY_ROUTE,
            R.string.cancellation_policy_title,
            cancellationSections
        )
    }
}

@Composable
internal fun CampsiteRulesLink(navController: NavController) {
    CampsiteInformationLink(navController, CAMPSITE_RULES_ROUTE, R.string.campsite_rules_link)
}

@Composable
internal fun CancellationPolicyLink(navController: NavController) {
    CampsiteInformationLink(navController, CANCELLATION_POLICY_ROUTE, R.string.cancellation_policy_link)
}

@Composable
private fun CampsiteInformationLink(navController: NavController, route: String, @StringRes label: Int) {
    Text(
        text = stringResource(label),
        modifier = Modifier.clickable(role = Role.Button) {
            navController.navigate(route) { launchSingleTop = true }
        },
        fontSize = 16.sp,
        color = Color.Blue
    )
}

@Composable
private fun CampsiteInformationScreen(
    navController: NavController,
    route: String,
    @StringRes title: Int,
    sections: List<Pair<Int, Int>>
) {
    val textColor = Color(0xFF303139)
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF4E8D5),
        contentColor = textColor,
        topBar = { TopBar(navController) },
        bottomBar = { CustomBottomBar(navController, currentRoute = route) }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(title),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.campsite_information_sample),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            sections.forEach { (sectionTitle, sectionBody) ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    contentColor = textColor
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(sectionTitle),
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(stringResource(sectionBody), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
