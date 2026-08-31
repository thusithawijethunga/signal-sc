package com.widhura.signalxp.ui

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
import com.widhura.signalxp.BuildConfig
import com.widhura.signalxp.data.api.AuthViewModel
import com.widhura.signalxp.ui.components.AddNewsDialog
import com.widhura.signalxp.ui.components.AddSignalDialog
import com.widhura.signalxp.ui.components.GeminiAnalysisDialog
import com.widhura.signalxp.ui.screens.AnalyticsSummaryScreen
import com.widhura.signalxp.ui.screens.CommunityScreen
import com.widhura.signalxp.ui.screens.DeveloperSettingsScreen
import com.widhura.signalxp.ui.screens.LoginScreen
import com.widhura.signalxp.ui.screens.MarketNewsScreen
import com.widhura.signalxp.ui.screens.SignalsFeedScreen
import com.widhura.signalxp.ui.screens.VipLeaderboardScreen
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardHeaderBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.SignalXpressTheme
import com.widhura.signalxp.ui.theme.TextSecondary

enum class NavTab { SIGNALS, SUMMARY, NEWS, COMMUNITY, VIP_LEADERBOARD }

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyScreenshotPolicy()
        enableEdgeToEdge()

        setContent {
            SignalXpressTheme {
                val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

                if (isLoggedIn) {
                    MainAppContent(viewModel = viewModel)
                } else {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = { /* isLoggedIn state handles navigation */ }
                    )
                }
            }
        }
    }

    private fun applyScreenshotPolicy() {
        if (BuildConfig.SCREENSHOT_DISABLED) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(NavTab.SIGNALS) }
    var showDeveloperSettings by remember { mutableStateOf(false) }

    if (showDeveloperSettings) {
        DeveloperSettingsScreen(
            viewModel = viewModel,
            onBack = { showDeveloperSettings = false }
        )
    } else {
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
                        viewModel = viewModel,
                        onTitleTap = { showDeveloperSettings = true }
                    )
                    NavTab.SUMMARY -> AnalyticsSummaryScreen(viewModel = viewModel)
                    NavTab.NEWS -> MarketNewsScreen(viewModel = viewModel)
                    NavTab.COMMUNITY -> CommunityScreen(viewModel = viewModel)
                    NavTab.VIP_LEADERBOARD -> VipLeaderboardScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(currentTab: NavTab, onTabSelected: (NavTab) -> Unit) {
    Surface(
        color = CardHeaderBackground,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column {
            Divider(color = BorderColor, thickness = 1.dp)
            Row(
                modifier = Modifier.fillMaxWidth().height(58.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(icon = "📡", label = "Signals", isSelected = currentTab == NavTab.SIGNALS) { onTabSelected(NavTab.SIGNALS) }
                NavItem(icon = "📊", label = "Summary", isSelected = currentTab == NavTab.SUMMARY) { onTabSelected(NavTab.SUMMARY) }
                NavItem(icon = "📰", label = "News", isSelected = currentTab == NavTab.NEWS) { onTabSelected(NavTab.NEWS) }
                NavItem(icon = "👥", label = "Community", isSelected = currentTab == NavTab.COMMUNITY) { onTabSelected(NavTab.COMMUNITY) }
                NavItem(icon = "🏆", label = "Top 10", isSelected = currentTab == NavTab.VIP_LEADERBOARD) { onTabSelected(NavTab.VIP_LEADERBOARD) }
            }
        }
    }
}

@Composable
fun NavItem(icon: String, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val tintColor = if (isSelected) PrimarySky else TextSecondary
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 17.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = tintColor, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
    }
}
