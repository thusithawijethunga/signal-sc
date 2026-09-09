package com.widhura.signalxp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.widhura.signalxp.BuildConfig
import com.widhura.signalxp.data.OnboardingPreferences
import com.widhura.signalxp.data.ThemePreferences
import com.widhura.signalxp.data.api.AuthViewModel
import com.widhura.signalxp.ui.screens.AnalyticsSummaryScreen
import com.widhura.signalxp.ui.screens.CommunityScreen
import com.widhura.signalxp.ui.screens.SettingsScreen
import com.widhura.signalxp.ui.screens.LoginScreen
import com.widhura.signalxp.ui.screens.MarketNewsScreen
import com.widhura.signalxp.ui.screens.OnboardingScreen
import com.widhura.signalxp.ui.screens.ProfileScreen
import com.widhura.signalxp.ui.screens.SignalsFeedScreen
import com.widhura.signalxp.ui.screens.SplashScreen
import com.widhura.signalxp.ui.screens.VipLeaderboardScreen
import com.widhura.signalxp.ui.theme.BorderColor
import com.widhura.signalxp.ui.theme.CardHeaderBackground
import com.widhura.signalxp.ui.theme.DarkBackground
import com.widhura.signalxp.ui.theme.LightTheme
import com.widhura.signalxp.ui.theme.PrimarySky
import com.widhura.signalxp.ui.theme.SignalXpressTheme
import com.widhura.signalxp.ui.theme.TextSecondary
import com.widhura.signalxp.NotificationForegroundService
import com.widhura.signalxp.data.api.ApiClient
import com.widhura.signalxp.data.api.FcmTokenRegistrar
import com.widhura.signalxp.util.SignalNotifications
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class NavTab { SIGNALS, SUMMARY, NEWS, COMMUNITY, VIP_LEADERBOARD }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var themePreferences: ThemePreferences
    private lateinit var onboardingPreferences: OnboardingPreferences

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            updateNotifBanner()
        }

    private var notifBannerVisible by mutableStateOf(false)

    private fun updateNotifBanner() {
        notifBannerVisible = !SignalNotifications.isPermissionGranted(this)
    }

    override fun onResume() {
        super.onResume()
        updateNotifBanner()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyScreenshotPolicy()
        enableEdgeToEdge()
        requestNotificationPermission()
        updateNotifBanner()
        requestBatteryOptimizationExemption()
        handleNotificationIntent(intent)
        SignalNotifications.createAllChannels(applicationContext)

        // Start foreground service immediately so WebSocket stays alive
        // even when the user minimizes or "closes" the app (like WhatsApp/Telegram).
        val userId = ApiClient.getCurrentUserId(this).toString()
        if (userId.isNotEmpty() && userId != "0") {
            NotificationForegroundService.start(this, userId)
        }

        // Minimize instead of close on back press (move task to back)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })

        themePreferences = ThemePreferences(applicationContext)
        onboardingPreferences = OnboardingPreferences(applicationContext)

        setContent {
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val onboardingDone by onboardingPreferences.isOnboardingDone.collectAsState(initial = false)
            var showSplash by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                delay(1200)
                showSplash = false
            }

            // Keep the backend's FCM device registry in sync so push
            // notifications arrive in any app state (open/closed/killed).
            var wasLoggedIn by remember { mutableStateOf(isLoggedIn) }
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    FcmTokenRegistrar.refresh(this@MainActivity)
                } else if (wasLoggedIn) {
                    FcmTokenRegistrar.unregister(this@MainActivity)
                }
                wasLoggedIn = isLoggedIn
            }

            SignalXpressTheme(darkTheme = isDarkMode) {
                when {
                    showSplash -> SplashScreen(isDarkMode = isDarkMode)
                    !isLoggedIn -> LoginScreen(
                        authViewModel = authViewModel,
                        onLoginSuccess = { /* isLoggedIn state handles navigation */ }
                    )
                    !onboardingDone -> OnboardingScreen(
                        isDarkMode = isDarkMode,
                        onComplete = {
                            scope.launch {
                                onboardingPreferences.setOnboardingDone()
                            }
                        }
                    )
                    else -> Column(modifier = Modifier.fillMaxSize()) {
                    if (notifBannerVisible) {
                        NotificationPermissionBanner(
                            onEnable = {
                                if (Build.VERSION.SDK_INT >= 33 &&
                                    androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                                        this@MainActivity,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                ) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    SignalNotifications.openNotificationSettings(this@MainActivity)
                                }
                            },
                            onDismiss = { notifBannerVisible = false }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                    MainAppContent(
                        viewModel = viewModel,
                        authViewModel = authViewModel,
                        isDarkMode = isDarkMode,
                        onToggleTheme = {
                            scope.launch {
                                themePreferences.setDarkMode(!isDarkMode)
                            }
                        }
                    )
                    }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val signalId = intent?.getLongExtra("signal_id", 0L) ?: 0L
        val signalNo = intent?.getIntExtra("signal_no", 0) ?: 0
        if (signalId != 0L || signalNo != 0) {
            viewModel.focusSignal(signalId, signalNo)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun requestBatteryOptimizationExemption() {
        val pm = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val pkg = packageName
        if (!pm.isIgnoringBatteryOptimizations(pkg)) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = android.net.Uri.parse("package:$pkg")
                }
                startActivity(intent)
            } catch (e: Exception) {
                // Some OEMs block this intent — fail silently, not critical path
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
fun MainAppContent(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel,
    isDarkMode: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    var currentTab by remember { mutableStateOf(NavTab.SIGNALS) }
    var showDeveloperSettings by remember { mutableStateOf(false) }
    var showProfile by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val bg = if (isDarkMode) DarkBackground else LightTheme.Background
    val headerBg = if (isDarkMode) CardHeaderBackground else LightTheme.CardHeaderBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val primary = if (isDarkMode) PrimarySky else LightTheme.PrimarySky

    val activeNotification by viewModel.activeNotification.collectAsState()

    // Auto-sync when app resumes to foreground
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncAllFromApi()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    LaunchedEffect(activeNotification) {
        activeNotification?.let {
            delay(4000)
            viewModel.clearNotification()
        }
    }

    when {
        showProfile -> ProfileScreen(
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            onBack = { showProfile = false },
            onSignOut = { authViewModel.logout() }
        )
        showSettings -> SettingsScreen(
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            onBack = { showSettings = false }
        )
        showDeveloperSettings -> SettingsScreen(
            isDarkMode = isDarkMode,
            onToggleTheme = onToggleTheme,
            onBack = { showDeveloperSettings = false }
        )
        else -> {
            Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bg,
            bottomBar = {
                BottomNavBar(
                    currentTab = currentTab,
                    isDarkMode = isDarkMode,
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
                        isDarkMode = isDarkMode,
                        onTitleTap = { showDeveloperSettings = true },
                        onProfileClick = { showProfile = true }
                    )
                    NavTab.SUMMARY -> AnalyticsSummaryScreen(viewModel = viewModel, isDarkMode = isDarkMode)
                    NavTab.NEWS -> MarketNewsScreen(viewModel = viewModel, isDarkMode = isDarkMode)
                    NavTab.COMMUNITY -> CommunityScreen(viewModel = viewModel, isDarkMode = isDarkMode)
                    NavTab.VIP_LEADERBOARD -> VipLeaderboardScreen(viewModel = viewModel, isDarkMode = isDarkMode)
                }

                // Real-time notification banner
                AnimatedVisibility(
                    visible = activeNotification != null,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    activeNotification?.let { notification ->
                        val notifBg = when (notification.type) {
                            "trade" -> Color(0xFF10B981)
                            "trade_hit" -> Color(0xFFF59E0B)
                            "signal_deleted", "signal_delete" -> Color(0xFFEF4444)
                            "signal_update" -> Color(0xFF3B82F6)
                            "signal_reaction" -> Color(0xFF8B5CF6)
                            "community" -> Color(0xFF06B6D4)
                            else -> primary
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = notifBg,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notification.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = notification.body,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun NotificationPermissionBanner(onEnable: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        color = Color(0xFFB45309),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Notifications off",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "You won't get signal alerts in background.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }
            Text(
                text = "ENABLE",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { onEnable() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(0.dp))
            Text(
                text = "✕",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { onDismiss() }
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
fun BottomNavBar(currentTab: NavTab, isDarkMode: Boolean = true, onTabSelected: (NavTab) -> Unit) {
    val bg = if (isDarkMode) CardHeaderBackground else LightTheme.CardHeaderBackground
    val border = if (isDarkMode) BorderColor else LightTheme.BorderColor
    val selectedColor = if (isDarkMode) PrimarySky else LightTheme.PrimarySky
    val unselectedColor = if (isDarkMode) TextSecondary else LightTheme.TextSecondary

    Surface(
        color = bg,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column {
            HorizontalDivider(Modifier, thickness = 1.dp, color = border)
            Row(
                modifier = Modifier.fillMaxWidth().height(58.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(icon = "\uD83D\uDCE1", label = "Signals", isSelected = currentTab == NavTab.SIGNALS, selectedColor = selectedColor, unselectedColor = unselectedColor) { onTabSelected(NavTab.SIGNALS) }
                NavItem(icon = "\uD83D\uDCCA", label = "Summary", isSelected = currentTab == NavTab.SUMMARY, selectedColor = selectedColor, unselectedColor = unselectedColor) { onTabSelected(NavTab.SUMMARY) }
                NavItem(icon = "\uD83D\uDCF0", label = "News", isSelected = currentTab == NavTab.NEWS, selectedColor = selectedColor, unselectedColor = unselectedColor) { onTabSelected(NavTab.NEWS) }
                NavItem(icon = "\uD83D\uDC65", label = "Community", isSelected = currentTab == NavTab.COMMUNITY, selectedColor = selectedColor, unselectedColor = unselectedColor) { onTabSelected(NavTab.COMMUNITY) }
                NavItem(icon = "\uD83C\uDFC6", label = "Top 10", isSelected = currentTab == NavTab.VIP_LEADERBOARD, selectedColor = selectedColor, unselectedColor = unselectedColor) { onTabSelected(NavTab.VIP_LEADERBOARD) }
            }
        }
    }
}

@Composable
fun NavItem(icon: String, label: String, isSelected: Boolean, selectedColor: androidx.compose.ui.graphics.Color = PrimarySky, unselectedColor: androidx.compose.ui.graphics.Color = TextSecondary, onClick: () -> Unit) {
    val tintColor = if (isSelected) selectedColor else unselectedColor
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
