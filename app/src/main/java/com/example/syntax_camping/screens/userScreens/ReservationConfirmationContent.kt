package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.syntax_camping.R

@Composable
internal fun ReservationConfirmationControls(
    navController: NavController,
    acknowledged: Boolean,
    onAcknowledgementChange: (Boolean) -> Unit,
    bookingNameEntered: Boolean,
    submitting: Boolean,
    onConfirm: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.reservation_rules_heading), style = MaterialTheme.typography.titleMedium)
        CampsiteRulesLink(navController)
        CancellationPolicyLink(navController)
        Row(
            modifier = Modifier.fillMaxWidth().testTag("agreeRulesCheckbox")
                .toggleable(
                    value = acknowledged,
                    enabled = !submitting,
                    role = Role.Checkbox,
                    onValueChange = onAcknowledgementChange
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = acknowledged, onCheckedChange = null, enabled = !submitting)
            Text(stringResource(R.string.reservation_rules_acknowledge), modifier = Modifier.weight(1f))
        }
        Button(
            onClick = onConfirm,
            enabled = acknowledged && bookingNameEntered && !submitting,
            modifier = Modifier.fillMaxWidth().testTag("confirmReservationButton"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
        ) {
            Text(stringResource(if (submitting) R.string.reservation_confirming else R.string.reservation_confirm))
        }
    }
}

@Composable
internal fun ReservationConfirmedContent(
    campsiteName: String,
    checkIn: String,
    checkOut: String,
    onReservations: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.reservation_confirmed), style = MaterialTheme.typography.headlineSmall)
        Text(campsiteName, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.reservation_check_in, checkIn))
        Text(stringResource(R.string.reservation_check_out, checkOut))
        Text(stringResource(R.string.reservation_confirmed_status))
        Button(
            onClick = onReservations,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3E6B68))
        ) {
            Text(stringResource(R.string.reservation_view_reservations))
        }
    }
}
