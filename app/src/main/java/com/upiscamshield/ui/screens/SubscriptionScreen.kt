package com.upiscamshield.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.upiscamshield.MainActivity
import com.upiscamshield.utils.BillingManager
import com.upiscamshield.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navController: NavController,
    viewModel: MainViewModel,
    billingManager: BillingManager
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val subscriptionState by viewModel.subscriptionState.collectAsState()
    val purchaseState by billingManager.purchaseState.collectAsState()
    var productDetails by remember { mutableStateOf<com.android.billingclient.api.ProductDetails?>(null) }
    
    LaunchedEffect(Unit) {
        productDetails = billingManager.loadProductDetails()
    }
    
    // Handle purchase success
    LaunchedEffect(purchaseState) {
        if (purchaseState is BillingManager.PurchaseState.Purchased) {
            viewModel.activateSubscription()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium Subscription") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (subscriptionState.isPremium) {
                // Already subscribed
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "You're Premium!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Enjoy unlimited scans and all premium features",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Not subscribed - show paywall
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Upgrade to Premium",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Get unlimited access to all features and protect yourself from UPI scams",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Features list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureItem(Icons.Default.QrCodeScanner, "Unlimited QR Code Scans")
                    FeatureItem(Icons.Default.Security, "Advanced Scam Detection")
                    FeatureItem(Icons.Default.PictureAsPdf, "PDF Report Export")
                    FeatureItem(Icons.Default.History, "Complete Scan History")
                    FeatureItem(Icons.Default.Lock, "App Lock Protection")
                    FeatureItem(Icons.Default.Support, "Priority Support")
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Price card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "₹499/year",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Just ₹41.58/month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Subscribe button
                Button(
                    onClick = {
                        activity?.let { act ->
                            productDetails?.let { details ->
                                billingManager.launchPurchaseFlow(act, details)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = productDetails != null && purchaseState !is BillingManager.PurchaseState.Pending
                ) {
                    when (purchaseState) {
                        is BillingManager.PurchaseState.Pending -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing...")
                        }
                        else -> {
                            Text(
                                text = "Subscribe Now",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                
                TextButton(onClick = { /* Restore purchases */ }) {
                    Text("Restore Purchase")
                }
            }
        }
    }
}

@Composable
fun FeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
