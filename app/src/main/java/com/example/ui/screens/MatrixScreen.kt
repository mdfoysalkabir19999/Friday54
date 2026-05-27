package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FridayViewModel
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlowBlue

@Composable
fun MatrixScreen(viewModel: FridayViewModel, paddingValues: PaddingValues) {
    val isCompilingUpgrade by viewModel.isCompilingUpgrade.collectAsState()
    val upgradeProgress by viewModel.upgradeProgress.collectAsState()
    val upgradeLogs by viewModel.upgradeLogLines.collectAsState()

    val emotionalResonance by viewModel.emotionalResonance.collectAsState()
    val analyticDepth by viewModel.analyticDepth.collectAsState()
    val witCoefficient by viewModel.witCoefficient.collectAsState()
    val clockSpeedHz by viewModel.synapseSpeedHz.collectAsState()

    // Mock Switches
    var biosyncOverride by remember { mutableStateOf(true) }
    var neuralCacheBypass by remember { mutableStateOf(false) }
    var forceSarcasm by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(CyberDark)
    ) {
        if (isCompilingUpgrade) {
            // --- FUTURISTIC MATRIX UPGRADE ONBOARDING VIEWER ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Icon(
                    imageVector = Icons.Default.Cached,
                    contentDescription = "Sync",
                    tint = CyberPrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "COMPILING CORE COGNITIVE UPGRADE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Text(
                    text = "Friday is modifying her logical arrays in storage. Please hold synapse connection.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                // Progress Bar
                LinearProgressIndicator(
                    progress = { upgradeProgress },
                    color = CyberPrimary,
                    trackColor = CyberCard,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
                )

                Text(
                    text = "Synaptic compilation: ${(upgradeProgress * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = CyberPrimary,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.End)
                )

                // Upgrade logs console
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, CyberCard, RoundedCornerShape(8.dp)),
                    color = CyberDark
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(upgradeLogs) { logLine ->
                            Text(
                                text = ">>> $logLine",
                                color = Color.Green,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        } else {
            // --- COGNITIVE ADJUSTMENT CONTROL MATRIX ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "FRIDAY BRAIN METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberCard, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = "Core Brain", tint = CyberPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "COGNITIVE SYNAPSE MATRIX",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Divider(color = CyberDark, thickness = 1.dp)

                        CognitiveMeterRow(
                            label = "EMOTIONAL RESONANCE (FRIENDLY)",
                            value = emotionalResonance,
                            glowColor = CyberSecondary
                        )

                        CognitiveMeterRow(
                            label = "ANALYTIC COGNITIVE DEPTH",
                            value = analyticDepth,
                            glowColor = CyberPrimary
                        )

                        CognitiveMeterRow(
                            label = "WIT & CHARISMA INDEX",
                            value = witCoefficient,
                            glowColor = GlowBlue
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SYSTEM SYNAPTIC CLOCK SPEED",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${String.format("%.2f", clockSpeedHz)} GHz",
                                color = Color.Green,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Privilege Switches
                Text(
                    text = "MOBILE HARDWARE INTERFACE OVERLAYS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SwitchPreferenceRow(
                        icon = Icons.Default.Security,
                        title = "Biosync Signature Handshake Bypass",
                        subtitle = "Overrides system credentials and pairs custom local hash codes.",
                        checked = biosyncOverride,
                        onCheckedChange = { biosyncOverride = it }
                    )

                    SwitchPreferenceRow(
                        icon = Icons.Default.DeveloperBoard,
                        title = "Quantum Cache Synchronization",
                        subtitle = "Streams deep response sequences directly into cold memory cache.",
                        checked = neuralCacheBypass,
                        onCheckedChange = { neuralCacheBypass = it }
                    )

                    SwitchPreferenceRow(
                        icon = Icons.Default.InstallMobile,
                        title = "Autonomous Android Core Integration",
                        subtitle = "Maintains persistent status notifications and schedules background checks.",
                        checked = forceSarcasm,
                        onCheckedChange = { forceSarcasm = it }
                    )
                }

                // Massive system upgrade trigger
                Button(
                    onClick = { viewModel.triggerSelfUpgrade() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("upgrade_button")
                ) {
                    Icon(Icons.Default.SystemUpdateAlt, contentDescription = "Upgrade core array", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UPGRADE FRIDAY MAIN MATRIX",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun CognitiveMeterRow(
    label: String,
    value: Float,
    glowColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.LightGray,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "${(value * 100).toInt()}%",
                color = glowColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { value },
            color = glowColor,
            trackColor = CyberDark,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun SwitchPreferenceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberCard, RoundedCornerShape(8.dp))
            .background(CyberCard.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = CyberSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberPrimary,
                checkedTrackColor = CyberPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color.Transparent
            )
        )
    }
}
