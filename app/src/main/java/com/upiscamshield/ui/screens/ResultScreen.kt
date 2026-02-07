package com.upiscamshield.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.upiscamshield.data.model.RiskLevel
import com.upiscamshield.ui.theme.*
import com.upiscamshield.utils.PDFExporter
import com.upiscamshield.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scanResult by viewModel.currentScanResult.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }
    
    if (scanResult == null) {
        LaunchedEffect(Unit) {
            navController.navigateUp()
        }
        return
    }
    
    val scan = scanResult!!
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Result") },
                navigationIcon = {
                    IconButton(onClick = { 
                        viewModel.resetScannerState()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            try {
                                val pdfFile = PDFExporter.exportScanToPDF(context, scan)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    pdfFile
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Open PDF"))
                                showExportDialog = true
                            } catch (e: Exception) {
                                // Handle error
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export PDF")
                }
                
                Button(
                    onClick = {
                        viewModel.resetScannerState()
                        navController.navigate(Screen.Scanner.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Another")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Risk Level Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = getRiskColor(scan.riskLevel).copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = getRiskIcon(scan.riskLevel),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = getRiskColor(scan.riskLevel)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = getRiskText(scan.riskLevel),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = getRiskColor(scan.riskLevel)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Risk Score: ${String.format("%.0f", scan.riskScore * 100)}%",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            // UPI Details Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "UPI Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailRow("UPI ID", scan.upiId)
                    scan.merchantName?.let { DetailRow("Merchant Name", it) }
                    scan.amount?.let { DetailRow("Amount", "₹$it") }
                    scan.transactionNote?.let { DetailRow("Transaction Note", it) }
                }
            }
            
            // Risk Factors Card
            if (scan.riskFactors.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Risk Factors",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        scan.riskFactors.forEach { factor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("• ", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = factor,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("PDF Exported") },
            text = { Text("The scan report has been exported as a PDF file.") },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

fun getRiskColor(riskLevel: RiskLevel) = when (riskLevel) {
    RiskLevel.SAFE -> RiskSafe
    RiskLevel.LOW -> RiskLow
    RiskLevel.MEDIUM -> RiskMedium
    RiskLevel.HIGH -> RiskHigh
    RiskLevel.CRITICAL -> RiskCritical
}

fun getRiskIcon(riskLevel: RiskLevel) = when (riskLevel) {
    RiskLevel.SAFE -> Icons.Default.CheckCircle
    RiskLevel.LOW -> Icons.Default.Info
    RiskLevel.MEDIUM -> Icons.Default.Warning
    RiskLevel.HIGH -> Icons.Default.Error
    RiskLevel.CRITICAL -> Icons.Default.Dangerous
}

fun getRiskText(riskLevel: RiskLevel) = when (riskLevel) {
    RiskLevel.SAFE -> "Safe"
    RiskLevel.LOW -> "Low Risk"
    RiskLevel.MEDIUM -> "Medium Risk"
    RiskLevel.HIGH -> "High Risk"
    RiskLevel.CRITICAL -> "Critical Risk"
}
