package com.example.syntax_camping.UserScreens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.syntax_camping.R

internal const val APP_GUIDELINES_ROUTE = "AppGuidelines"

// Prototype copy following the supplied storyboard, pending the team's approved policy.
// The displayed revision date comes from that reference, not a backend policy record.
private val policySections = listOf(
    "Introduction" to "Syntax Camping helps campers browse campsites and campsite owners share site information. Read these guidelines before using the app and follow the rules posted for each campsite.",
    "Information We Collect" to "App features may use profile details you provide, campsite information, photos, ratings and participation in campsite waitlists or lotteries. Avoid including sensitive personal details in public content.",
    "How We Use Your Information" to "Information is used to display campsite details and support the app features you choose, such as managing a profile or participating in a campsite waitlist. Keep information you submit accurate.",
    "Information Sharing" to "Consider who can view a campsite listing or other content before submitting it. Do not publish another person’s contact details or photos without their permission.",
    "Data Security" to "Protect access to your device and avoid sharing private account information. Report suspected misuse to the project team and take care when using a shared device.",
    "Cookies & Similar Technologies" to "Review your device and app settings when managing saved preferences. On a shared device, consider whether remembering account information is appropriate.",
    "Your Rights" to "Review the information you submit and use available profile controls to keep it current. Contact the project team with questions about access, correction or removal of personal information.",
    "Data Retention" to "Avoid submitting information that is unnecessary for using the app. Contact the project team to ask how stored information is kept or to request removal of information you have provided."
)

@Composable
fun AppGuidelinesScreen(navController: NavController) {
    var expandedSection by rememberSaveable { mutableIntStateOf(-1) }
    SettingsPagesLayout(navController, backgroundColor = Color(0xFFF2F2F8)) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Syntax Camping", color = Color(0xFF99999F), fontSize = 12.sp)
                Text("Privacy Policy", modifier = Modifier.semantics { heading() },
                    fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text("Updated January 30, 2026", color = Color(0xFF99999F), fontSize = 12.sp)
            }
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFE6F0FF)) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Canvas(Modifier.padding(top = 2.dp).size(16.dp, 18.dp)) {
                        val shield = Path().apply {
                            moveTo(size.width * .5f, 1f)
                            lineTo(size.width * .85f, size.height * .2f)
                            lineTo(size.width * .85f, size.height * .55f)
                            quadraticTo(size.width * .85f, size.height * .8f, size.width * .5f, size.height - 1f)
                            quadraticTo(size.width * .15f, size.height * .8f, size.width * .15f, size.height * .55f)
                            lineTo(size.width * .15f, size.height * .2f)
                            close()
                        }
                        drawPath(shield, Color(0xFF5498F5), style = Stroke(width = 1.8.dp.toPx()))
                    }
                    Text("We are committed to protecting your personal data. This policy covers how we collect, use, and safeguard your information.",
                        fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF55555D))
                }
            }
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column {
                    policySections.forEachIndexed { index, (title, body) ->
                        val expanded = expandedSection == index
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable(role = Role.Button) { expandedSection = if (expanded) -1 else index }
                                    .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                                    .heightIn(min = 46.dp).padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text("${index + 1}", color = Color(0xFFCECED4), fontSize = 12.sp)
                                Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp, lineHeight = 20.sp)
                                Icon(painterResource(R.drawable.forward), null,
                                    modifier = Modifier.size(7.dp, 11.dp).rotate(if (expanded) 90f else 0f),
                                    tint = Color(0xFFCCCCD3))
                            }
                            if (expanded) Text(body, modifier = Modifier.padding(start = 38.dp, end = 16.dp, bottom = 16.dp),
                                color = Color(0xFF55555D), fontSize = 14.sp, lineHeight = 21.sp)
                            if (index < policySections.lastIndex) HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp), color = Color(0xFFF0F0F5))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 890)
@Composable
private fun AppGuidelinesPreview() { AppGuidelinesScreen(rememberNavController()) }
