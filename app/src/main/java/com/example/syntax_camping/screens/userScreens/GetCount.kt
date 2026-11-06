import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.syntax_camping.dataClasses.Lottery
import com.example.syntax_camping.dataClasses.lotteryRepository

@Composable
fun getLotteryCounts(lottery: Lottery?): Pair<Int, Int> {
    var entryCount by remember { mutableStateOf(0) }
    var waitlistCount by remember { mutableStateOf(0) }

    // Whenever the lottery ID changes, re-fetch the counts
    LaunchedEffect(lottery?.lotteryId) {
        lottery?.lotteryId?.takeIf { it.isNotBlank() }?.let { id ->
            val lotteryFolder = lotteryRepository() // Actually initialize the repo

            lotteryFolder.getEntries(id) { entries ->
                entryCount = entries.size
            }
            lotteryFolder.getWaitlist(id) { waitlist ->
                waitlistCount = waitlist.size
            }
        }
    }

    // Return both numbers packed together
    return Pair(entryCount, waitlistCount)
}