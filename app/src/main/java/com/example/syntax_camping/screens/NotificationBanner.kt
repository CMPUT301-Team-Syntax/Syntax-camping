package com.example.syntax_camping.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.syntax_camping.NotificationInbox
import com.example.syntax_camping.R

@Composable
internal fun NotificationBannerHost(
    inbox: NotificationInbox,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier) {
        content()
        inbox.banner?.let { notification ->
            key(notification.id) {
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = Color(0xFF373F48),
                    contentColor = Color.White,
                    shadowElevation = 6.dp
                ) {
                    Row(modifier = Modifier.padding(start = 16.dp, end = 4.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_notification_info),
                            contentDescription = null,
                            modifier = Modifier.padding(top = 16.dp).size(22.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f).heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(start = 12.dp, top = 16.dp, bottom = 16.dp)
                        ) {
                            Text(
                                text = notification.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = notification.message,
                                modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        IconButton(onClick = { inbox.dismissBanner(notification.id) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_notification_close),
                                contentDescription = stringResource(R.string.notification_dismiss)
                            )
                        }
                    }
                }
            }
        }
    }
}
