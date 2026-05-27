package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.VaultItemEntity
import com.example.ui.FridayViewModel
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlowBlue
import com.example.ui.theme.WarningRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaultScreen(viewModel: FridayViewModel, paddingValues: PaddingValues) {
    val vaultAuthenticated by viewModel.isVaultAuthenticated.collectAsState()
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val vaultItems by viewModel.vaultItems.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newSecretText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(CyberDark)
    ) {
        if (!vaultAuthenticated) {
            // --- SECURITY SHIELD LAYER ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Shield Locked",
                    tint = WarningRed,
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SECURE MEMORY ENVELOPE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "RESTRICTED ACCESS: MASTER SIGNATURE MANDATED",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Fingerprint animation ring
                    BiometricHandshakeScannerRing(isScanning = isAuthenticating)

                    IconButton(
                        onClick = { viewModel.executeBiometricScan {} },
                        enabled = !isAuthenticating,
                        modifier = Modifier
                            .size(90.dp)
                            .background(if (isAuthenticating) CyberPrimary.copy(alpha = 0.2f) else CyberCard, CircleShape)
                            .border(1.dp, if (isAuthenticating) CyberPrimary else Color.Gray.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isAuthenticating) Icons.Default.SettingsVoice else Icons.Default.Fingerprint,
                            contentDescription = "Execute handshake biometric sensor",
                            tint = if (isAuthenticating) CyberPrimary else Color.White,
                            modifier = Modifier.size(45.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = { viewModel.executeBiometricScan {} },
                    enabled = !isAuthenticating,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAuthenticating) CyberPrimary.copy(alpha = 0.2f) else CyberPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    if (isAuthenticating) {
                        CircularProgressIndicator(color = CyberPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("VERIFYING HANDSHAKE HASH...", color = CyberPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    } else {
                        Text("DISPATCH BIOMETRIC SCAN", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            // --- ACCESS GRANTED VAULT SCREEN ---
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SECURED DATA VAULT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Absolute clearance encryption wrapping: online",
                            fontSize = 9.sp,
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Button(
                        onClick = { viewModel.lockVault() },
                        colors = ButtonDefaults.buttonColors(containerColor = WarningRed.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, WarningRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = "Lock", tint = WarningRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LOCK", color = WarningRed, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of secured items
                if (vaultItems.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FolderSpecial, contentDescription = "Empty vault", tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Secure vault is currently empty, Sir.",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(vaultItems, key = { it.id }) { item ->
                            VaultSecretItemCard(item = item, onDelete = { viewModel.deleteVaultItem(item.id, item.title) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Float action styled insert triggers
                Button(
                    onClick = { showAddItemDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add secured element", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADD SECURED RECORD", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // --- ADD DIALOG FORM ---
        if (showAddItemDialog) {
            AlertDialog(
                onDismissRequest = { showAddItemDialog = false },
                containerColor = CyberCard,
                modifier = Modifier.border(1.dp, CyberPrimary.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
                title = {
                    Text(
                        "ADD ENCRYPTED RECORD",
                        color = CyberPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "This record will be written to local private SQLite storage with 256-bit encryption wrap.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        TextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("Title (e.g. Boss's Private API Key)", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = CyberDark,
                                unfocusedContainerColor = CyberDark,
                                focusedIndicatorColor = CyberPrimary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("vault_item_title")
                        )

                        TextField(
                            value = newSecretText,
                            onValueChange = { newSecretText = it },
                            placeholder = { Text("Secret Value (e.g. content values...)", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = CyberDark,
                                unfocusedContainerColor = CyberDark,
                                focusedIndicatorColor = CyberPrimary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("vault_item_content")
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newTitle.isNotBlank() && newSecretText.isNotBlank()) {
                                viewModel.addVaultItem(newTitle, newSecretText)
                                newTitle = ""
                                newSecretText = ""
                                showAddItemDialog = false
                            }
                        }
                    ) {
                        Text("SAVE RECORD", color = CyberPrimary, fontFamily = FontFamily.Monospace)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddItemDialog = false }) {
                        Text("CANCEL", color = Color.Gray, fontFamily = FontFamily.Monospace)
                    }
                }
            )
        }
    }
}

@Composable
fun BiometricHandshakeScannerRing(isScanning: Boolean) {
    if (isScanning) {
        val infiniteTransition = rememberInfiniteTransition(label = "scan_ring")
        val pulseRadius by infiniteTransition.animateFloat(
            initialValue = 70.dp.value,
            targetValue = 130.dp.value,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radius"
        )
        val ringAlpha by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "alpha"
        )

        Canvas(modifier = Modifier.size(150.dp)) {
            drawCircle(
                color = CyberPrimary.copy(alpha = ringAlpha),
                radius = pulseRadius.dp.toPx(),
                style = Stroke(width = 3.dp.toPx())
            )
        }
    } else {
        // Subtle constant rotation or static elements
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(CyberPrimary.copy(alpha = 0.05f), CircleShape)
                .blur(10.dp)
        )
    }
}

@Composable
fun VaultSecretItemCard(item: VaultItemEntity, onDelete: () -> Unit) {
    var revealed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberCard, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = "Secure Key", tint = CyberPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (revealed) item.secretContent else "••••••••••••••••••••",
                    color = if (revealed) GlowBlue else Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(onClick = { revealed = !revealed }) {
                Icon(
                    imageVector = if (revealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Reveal",
                    tint = Color.LightGray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Erase secured entity",
                    tint = WarningRed
                )
            }
        }
    }
}
