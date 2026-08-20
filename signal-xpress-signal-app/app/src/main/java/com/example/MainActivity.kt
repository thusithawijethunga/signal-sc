package com.example.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SignalEntity
import com.example.ui.components.AddNewsDialog
import com.example.ui.components.AddSignalDialog
import com.example.ui.components.GeminiAnalysisDialog
import com.example.ui.screens.AnalyticsSummaryScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.MarketNewsScreen
import com.example.ui.screens.SignalsFeedScreen
import com.example.ui.screens.VipLeaderboardScreen
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardHeaderBackground
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.PrimarySky
import com.example.ui.theme.SignalXpressTheme
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextSecondary

enum class NavTab { SIGNALS, SUMMARY, NEWS, COMMUNITY, VIP_LEADERBOARD }

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()

        setContent {
            SignalXpressTheme {
                var currentTab by remember { mutableStateOf(NavTab.SIGNALS) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkBackground,
                    bottomBar = {
                        BottomNavBar(
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavTab.SIGNALS -> SignalsFeedScreen(
                                viewModel = viewModel
                            )

                            NavTab.SUMMARY -> AnalyticsSummaryScreen(viewModel = viewModel)

                            NavTab.NEWS -> MarketNewsScreen(
                                viewModel = viewModel
                            )

                            NavTab.COMMUNITY -> CommunityScreen(
                                viewModel = viewModel
                            )

                            NavTab.VIP_LEADERBOARD -> VipLeaderboardScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    Surface(
        color = CardHeaderBackground,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column {
            Divider(color = BorderColor, thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = "📡",
                    label = "Signals",
                    isSelected = currentTab == NavTab.SIGNALS
                ) { onTabSelected(NavTab.SIGNALS) }

                NavItem(
                    icon = "📊",
                    label = "Summary",
                    isSelected = currentTab == NavTab.SUMMARY
                ) { onTabSelected(NavTab.SUMMARY) }

                NavItem(
                    icon = "📰",
                    label = "News",
                    isSelected = currentTab == NavTab.NEWS
                ) { onTabSelected(NavTab.NEWS) }

                NavItem(
                    icon = "👥",
                    label = "Community",
                    isSelected = currentTab == NavTab.COMMUNITY
                ) { onTabSelected(NavTab.COMMUNITY) }

                NavItem(
                    icon = "🏆",
                    label = "Top 10",
                    isSelected = currentTab == NavTab.VIP_LEADERBOARD
                ) { onTabSelected(NavTab.VIP_LEADERBOARD) }
            }
        }
    }
}

@Composable
fun NavItem(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tintColor = if (isSelected) PrimarySky else TextSecondary

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 17.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = tintColor,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
