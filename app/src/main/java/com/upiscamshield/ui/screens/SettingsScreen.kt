package com.upiscamshield.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import com.upiscamshield.BuildConfig
import com.upiscamshield.utils.BiometricHelper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val appLockEnabledKey = booleanPreferencesKey("app_lock_enabled")
    val appLockEnabled by context.settingsDataStore.data
        .map { it[appLockEnabledKey] ?: false }
        .collectAsState(initial = false)
    
    val isBiometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Security Section
            SettingsSection(title = "Security")
            
            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Enable App Lock",
                description = "Secure app with biometric authentication",
                enabled = isBiometricAvailable,
                trailing = {
                    Switch(
                        checked = appLockEnabled,
                        enabled = isBiometricAvailable,
                        onCheckedChange = { enabled ->
                            scope.launch {
                                context.settingsDataStore.edit { prefs ->
                                    prefs[appLockEnabledKey] = enabled
                                }
                            }
                        }
                    )
                }
            )
            
            if (!isBiometricAvailable) {
                Text(
                    text = "Biometric authentication is not available on this device",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Divider()
            
            // About Section
            SettingsSection(title = "About")
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Version",
                description = BuildConfig.VERSION_NAME,
                onClick = {}
            )
            
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                description = "View our privacy policy",
                onClick = {
                    // Open privacy policy
                }
            )
            
            SettingsItem(
                icon = Icons.Default.Description,
                title = "Terms of Service",
                description = "View terms of service",
                onClick = {
                    // Open terms of service
                }
            )
        }
    }
}

@Composable
fun SettingsSection(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { if (enabled && onClick != null) onClick() },
        enabled = enabled && onClick != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}
