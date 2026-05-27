package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AutonomousLogEntity
import com.example.ui.FridayViewModel
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlowBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogsScreen(viewModel: FridayViewModel, paddingValues: PaddingValues) {
    val logs by viewModel.autonomousLogs.collectAsState()

    var activeSEOThreads by remember { mutableIntStateOf(12) }
    var intrusionAttemptsBlocked by remember { mutableIntStateOf(44) }

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty() && Random.nextFloat() > 0.7f) {
            activeSEOThreads = (activeSEOThreads + Random.nextInt(-2, 3)).coerceIn(4, 25)
            intrusionAttemptsBlocked += Random.nextInt(1, 3)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(CyberDark)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            // --- HEADER STATISTICS MATRIX ---
            Text(
                text = "MAIN MATRIX TERMINAL STATUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberPrimary,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                TerminalStatusCard(
                    title = "TOTAL AGENCY LOGS",
                    value = "${logs.size} ops",
                    accentColor = CyberPrimary,
                    modifier = Modifier.weight(1f)
                )
                TerminalStatusCard(
                    title = "SEO INDEX SPIDERS",
                    value = "$activeSEOThreads spiders",
                    accentColor = CyberSecondary,
                    modifier = Modifier.weight(1f)
                )
                TerminalStatusCard(
                    title = "TRACE BLOCKED",
                    value = "$intrusionAttemptsBlocked threats",
                    accentColor = GlowBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TERMINAL VIEW ---
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                color = CyberDark
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    // CRT Title Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYSTEM AUTONOMOUS CONSOLE",
                            color = CyberPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberPrimary)
                        )
                    }

                    Divider(color = CyberPrimary.copy(alpha = 0.2f), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(8.dp))

                    if (logs.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No active logs indexed, waiting for cyber pulse...",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(logs, key = { it.id }) { logItem ->
                                LogTerminalRow(logItem = logItem)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- TERMINAL ACTIONS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        // Insert an prompt manual diagnostic log
                        viewModel.executeBiometricScan {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CyberSecondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Manual trace", tint = CyberSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TRIGGER MANUAL TRACE", color = CyberSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = { viewModel.clearAllAutonomousLogs() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Wipe CRT logs", tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WIPE TERMINAL", color = Color.LightGray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun TerminalStatusCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, com.example.ui.theme.Slate700.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = com.example.ui.theme.CyberSurface.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.LightGray.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun LogTerminalRow(logItem: AutonomousLogEntity) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeStr = formatter.format(Date(logItem.timestamp))

    val catColor = when (logItem.category) {
        "SECURITY" -> GlowBlue
        "SEO_AGENCY" -> CyberSecondary
        "OPTIMIZATION" -> CyberPrimary
        "CREATOR_DEFENSE" -> Color.Green
        else -> Color.White.copy(alpha = 0.7f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "[$timeStr]",
            color = Color.Gray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(68.dp)
        )

        Text(
            text = "${logItem.category}:",
            color = catColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(105.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = logItem.messageText,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 15.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
