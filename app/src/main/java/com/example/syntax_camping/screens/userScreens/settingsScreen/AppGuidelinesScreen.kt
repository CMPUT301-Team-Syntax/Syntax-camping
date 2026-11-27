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

// New separate list for the community guidelines, shown in its own block below the privacy policy
private val guidelineSections = listOf(
    "Guidelines Overview" to "These guidelines apply to everyone using Syntax Camping. They are split into rules for entrants, rules for campsite owners, and what happens when rules are broken.",
    "Entrants: Respect and Safety" to "Allowed:\n• Disagreeing politely\n• Asking an owner a question\n• Reporting a problem\n\nNot allowed:\n• Insults, harassment or hate speech\n• Threats\n• Repeated unwanted messages to an owner",
    "Entrants: Honesty" to "Allowed:\n• One account with your real name and contact details\n\nNot allowed:\n• Fake accounts\n• Entering a lottery with several accounts\n• Fake ratings or reviews, including for a friend's campsite",
    "Entrants: Content" to "Applies to profile images, reviews and messages.\n\nAllowed:\n• A photo of you or an avatar\n• Honest reviews about your stay\n\nNot allowed:\n• Explicit or violent images\n• Illegal content\n• Someone else's private details or photos without their permission",
    "Entrants: Reservations" to "Allowed:\n• Cancelling or declining a reservation, including at the last minute if your plans change\n\nNot allowed:\n• Repeatedly booking and not showing up\n• Holding dates you never plan to use",
    "Entrants: Reviews and Ratings" to "Allowed:\n• True negative reviews about the campsite, such as cleanliness, noise or safety\n\nNot allowed:\n• Reviewing a stay you did not have\n• Personal attacks on owners or other campers\n• Ads or links",
    "Owners: Listings" to "Allowed:\n• Accurate name, location, occupancy, price, rules and cancellation policy\n• A map pin at the real campsite\n\nNot allowed:\n• False amenities\n• Wrong map locations\n• Listing a site you do not own or that does not exist",
    "Owners: Lotteries" to "Allowed:\n• Setting the registration period, booking period and number of winners\n• Letting the draw run automatically\n\nNot allowed:\n• Choosing friends as winners\n• Changing the draw after it runs\n• Excluding entrants for personal reasons",
    "Owners: Rejecting Reservations" to "Allowed:\n• Rejecting for a real reason, such as a fire warning, flooding, a closure or unsafe conditions\n\nNot allowed:\n• Rejecting only to rebook the dates at a higher price",
    "Owners: Messages and Notifications" to "Allowed:\n• Reservation updates\n• Arrival instructions\n• Answering an entrant's questions\n\nNot allowed:\n• Spam or advertising\n• Asking for payment outside the app",
    "Owners: Images" to "Allowed:\n• Photos of your own campsite as it is\n\nNot allowed:\n• Stock photos or another campsite's images presented as yours\n• Edited images that mislead",
    "Warnings and Removal" to "Minor violation (for example an off-topic review or one rude message):\n• You receive a warning and the content is removed\n\nRepeated violations:\n• After five warnings, the account is deleted\n\nSerious violation (threats, fraud, fake listings, illegal content):\n• Immediate removal of the account, with no warnings\n\nCampsite owners:\n• A single false or unsafe listing can be removed on its own\n• An owner with a pattern of violations loses their account and all their campsites",
    "Questions and Removals" to "If you think content or an account was removed by mistake, contact the project team and explain what happened."
)

@Composable
fun AppGuidelinesScreen(navController: NavController) {
    var expandedSection by rememberSaveable { mutableIntStateOf(-1) } // privacy policy block: which section is open
    var expandedGuideline by rememberSaveable { mutableIntStateOf(-1) } // guidelines block: which section is open (separate so each block opens on its own)
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 16.dp)) { // new heading block for the guidelines
                Text("Syntax Camping", color = Color(0xFF99999F), fontSize = 12.sp) // same small label as the privacy header
                Text("Community Guidelines", modifier = Modifier.semantics { heading() }, // guidelines title
                    fontSize = 24.sp, fontWeight = FontWeight.Bold) // same style as the privacy title
                Text("Updated January 30, 2026", color = Color(0xFF99999F), fontSize = 12.sp) // change this date if the guidelines are newer
            }
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFE6F0FF)) { // new info box for the guidelines
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { // same layout as the privacy info box
                    Canvas(Modifier.padding(top = 2.dp).size(16.dp, 18.dp)) { // same shield icon
                        val shield = Path().apply { // draw the shield outline
                            moveTo(size.width * .5f, 1f)
                            lineTo(size.width * .85f, size.height * .2f)
                            lineTo(size.width * .85f, size.height * .55f)
                            quadraticTo(size.width * .85f, size.height * .8f, size.width * .5f, size.height - 1f)
                            quadraticTo(size.width * .15f, size.height * .8f, size.width * .15f, size.height * .55f)
                            lineTo(size.width * .15f, size.height * .2f)
                            close()
                        }
                        drawPath(shield, Color(0xFF5498F5), style = Stroke(width = 1.8.dp.toPx())) // blue outline
                    }
                    Text("These guidelines explain what is and is not allowed for entrants and campsite owners, and what happens when rules are broken.", // guidelines info text
                        fontSize = 13.sp, lineHeight = 19.sp, color = Color(0xFF55555D))
                }
            }
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White) { // new white card for the guideline sections
                Column {
                    guidelineSections.forEachIndexed { index, (title, body) -> // loops over the guidelines list instead of the policy list
                        val expanded = expandedGuideline == index // uses the guidelines' own open-section state
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable(role = Role.Button) { expandedGuideline = if (expanded) -1 else index } // toggles the guidelines' state
                                    .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" }
                                    .heightIn(min = 46.dp).padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text("${index + 1}", color = Color(0xFFCECED4), fontSize = 12.sp) // numbering restarts at 1 here
                                Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp, lineHeight = 20.sp)
                                Icon(painterResource(R.drawable.forward), null,
                                    modifier = Modifier.size(7.dp, 11.dp).rotate(if (expanded) 90f else 0f),
                                    tint = Color(0xFFCCCCD3))
                            }
                            if (expanded) Text(body, modifier = Modifier.padding(start = 38.dp, end = 16.dp, bottom = 16.dp),
                                color = Color(0xFF55555D), fontSize = 14.sp, lineHeight = 21.sp)
                            if (index < guidelineSections.lastIndex) HorizontalDivider( // divider uses the guidelines list length
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