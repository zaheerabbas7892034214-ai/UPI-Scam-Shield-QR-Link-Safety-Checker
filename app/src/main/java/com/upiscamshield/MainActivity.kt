package com.upiscamshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.upiscamshield.ui.screens.*
import com.upiscamshield.ui.theme.UpiScamShieldTheme
import com.upiscamshield.utils.BiometricHelper
import com.upiscamshield.utils.BillingManager
import com.upiscamshield.viewmodel.MainViewModel
import kotlinx.coroutines.flow.map

class MainActivity : FragmentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private lateinit var billingManager: BillingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize billing
        billingManager = BillingManager(this)
        billingManager.initialize()
        
        setContent {
            UpiScamShieldTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppLockWrapper {
                        MainApp(viewModel, billingManager)
                    }
                }
            }
        }
    }
    
    @Composable
    fun AppLockWrapper(content: @Composable () -> Unit) {
        val appLockEnabledKey = booleanPreferencesKey("app_lock_enabled")
        val appLockEnabled by applicationContext.settingsDataStore.data
            .map { it[appLockEnabledKey] ?: false }
            .collectAsState(initial = false)
        
        var isAuthenticated by remember { mutableStateOf(!appLockEnabled) }
        
        if (appLockEnabled && !isAuthenticated) {
            // Show authentication screen
            LaunchedEffect(Unit) {
                BiometricHelper.authenticate(
                    activity = this@MainActivity,
                    title = "Authenticate",
                    subtitle = "Verify your identity",
                    description = "Use biometric to unlock the app",
                    onSuccess = { isAuthenticated = true },
                    onError = { finishAffinity() }
                )
            }
        } else {
            content()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        billingManager.endConnection()
    }
}

@Composable
fun MainApp(viewModel: MainViewModel, billingManager: BillingManager) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController, viewModel)
        }
        composable(Screen.Scanner.route) {
            ScannerScreen(navController, viewModel)
        }
        composable(Screen.Result.route) {
            ResultScreen(navController, viewModel)
        }
        composable(Screen.History.route) {
            HistoryScreen(navController, viewModel)
        }
        composable(Screen.Subscription.route) {
            SubscriptionScreen(navController, viewModel, billingManager)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}

private val android.content.Context.settingsDataStore by androidx.datastore.preferences.preferencesDataStore(name = "settings")
