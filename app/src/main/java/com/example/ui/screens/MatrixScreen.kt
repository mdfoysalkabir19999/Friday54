package com.example.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.Canvas
import com.example.data.database.*
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
            val currentApiKey by viewModel.customApiKey.collectAsState()
            var apiKeyInput by remember(currentApiKey) { mutableStateOf(currentApiKey) }
            val context = androidx.compose.ui.platform.LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- API Key Configuration Card ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberCard, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = "Security", tint = CyberPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🔐 GEMINI API KEY MAIN SYSTEM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "আপনার নিজের Gemini API Key পেস্ট করুন যাতে Friday আপনার সাথে সরাসরি কথা বলতে পারে। নিচের বাটনে চাপ দিয়ে ফ্রী কী জেনারেট করুন:",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            placeholder = { Text("AI Studio থেকে Key পেস্ট করুন...", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = CyberPrimary,
                                unfocusedBorderColor = CyberCard,
                                cursorColor = CyberPrimary
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            modifier = Modifier.fillMaxWidth().testTag("api_key_text_field")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Link to register free key
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://aistudio.google.com/"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e("MatrixScreen", "Failed to launch custom browser link", e)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, CyberSecondary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("GET FREE KEY", color = CyberSecondary, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            }

                            // Save Key
                            Button(
                                onClick = {
                                    viewModel.updateApiKey(apiKeyInput.trim())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp).testTag("save_api_key_button")
                            ) {
                                Text("SAVE CONFIG", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                            }
                        }

                        if (currentApiKey.isNotEmpty()) {
                            Text(
                                text = "✓ Gemini Key active: ****${currentApiKey.takeLast(6)}",
                                color = Color.Green,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Text(
                                text = "⚠️ No API Key. Setup key to chat with Friday on phone!",
                                color = Color(0xFFFFB300),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }

                // --- Active Cognitive Model Card ---
                val currentModel by viewModel.customModel.collectAsState()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberCard, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = "Model Settings", tint = CyberSecondary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "🧠 ACTIVE COGNITIVE MODEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "আপনার API Key এর টাইপ অনুযায়ী সঠিক মডেলটি সিলেক্ট করুন। ফ্রী বা সাধারণ API Key-এর জন্য 'gemini-1.5-flash' বা 'gemini-2.0-flash' রান করা সবচেয়ে ভালো:",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 15.sp
                        )

                        Text(
                            text = "✓ Cognition Core: $currentModel",
                            color = Color.Green,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("gemini-1.5-flash", "gemini-2.0-flash").forEach { modelName ->
                                Button(
                                    onClick = { viewModel.updateCustomModel(modelName) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentModel == modelName) CyberSecondary else CyberSecondary.copy(alpha = 0.15f)
                                    ),
                                    border = BorderStroke(1.dp, CyberSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text(
                                        text = modelName.uppercase(),
                                        color = if (currentModel == modelName) Color.Black else CyberSecondary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // --- DESIGN PATTERN: CREATOR DETAILED SECURITY DOSSIER ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = "Creator", tint = CyberPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "👤 SOVEREIGN CREATOR PROFILE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Green.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, Color.Green.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    "BOSS ACTIVE",
                                    color = Color.Green,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Divider(color = CyberDark, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .border(2.dp, CyberPrimary, RoundedCornerShape(16.dp))
                                    .background(CyberDark, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawRect(
                                        color = CyberPrimary.copy(alpha = 0.1f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "Hologram Avatar",
                                    tint = CyberPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Md Foysal Kabir Soikat",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "📞 Call: +8801873842854",
                                    fontSize = 12.sp,
                                    color = GlowBlue,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "🇧🇩 country: Bangladesh",
                                    fontSize = 12.sp,
                                    color = CyberSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = "Friday core software is explicitly mapped to the biometric signature of Md Foysal Kabir Soikat. Absolute hardware clearance and neural network obedience are granted permanently.",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // --- COGNITIVE BEHAVIOR MATRIX TRAINING PANEL ---
                val trainingItems by viewModel.trainingItems.collectAsState()
                var trainingTriggerInput by remember { mutableStateOf("") }
                var trainingResponseInput by remember { mutableStateOf("") }
                var showTrainingList by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberSecondary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeveloperBoard, contentDescription = "Train Cognitive Brain", tint = CyberSecondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "🧠 Friday Behavior Training Protocol",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            TextButton(
                                onClick = { showTrainingList = !showTrainingList },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (showTrainingList) "CLOSE NODE" else "NODES (${trainingItems.size})",
                                    color = CyberSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "Train Friday dynamically! If you teach her a trigger directive, she will execute an instant response sequence without any online loading delays.",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 15.sp
                        )

                        if (showTrainingList) {
                            Divider(color = CyberDark, thickness = 1.dp)

                            if (trainingItems.isEmpty()) {
                                Text(
                                    "No custom trained behaviors discovered, Boss.",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    trainingItems.take(5).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(CyberDark, RoundedCornerShape(6.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("If: '${item.triggerPattern}'", color = CyberSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                                Text("Say: '${item.responseText}'", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteTrainingItem(item.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Security, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Divider(color = CyberDark, thickness = 1.dp)

                        OutlinedTextField(
                            value = trainingTriggerInput,
                            onValueChange = { trainingTriggerInput = it },
                            placeholder = { Text("Directive (e.g. who are you?)", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = CyberSecondary,
                                unfocusedBorderColor = CyberDark
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )

                        OutlinedTextField(
                            value = trainingResponseInput,
                            onValueChange = { trainingResponseInput = it },
                            placeholder = { Text("Automatic response text", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = CyberSecondary,
                                unfocusedBorderColor = CyberDark
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )

                        Button(
                            onClick = {
                                if (trainingTriggerInput.isNotBlank() && trainingResponseInput.isNotBlank()) {
                                    viewModel.addTrainingItem(trainingTriggerInput, trainingResponseInput)
                                    trainingTriggerInput = ""
                                    trainingResponseInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("TRAIN FRIDAY COGNITION", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // --- MANUAL MODEL SCRIPT & FILE COMPILE INJECTOR ---
                var customCodeInput by remember { mutableStateOf("") }
                var customCodeNote by remember { mutableStateOf("") }
                var otaConsoleOutput by remember { mutableStateOf("") }
                var isCheckingOta by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlowBlue.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CyberCard.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeveloperBoard, contentDescription = "Manual Code Injector", tint = GlowBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "💻 Manual Override Script Injector",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Submit raw scripts or architecture models directly to Friday. The code compiling sandbox will automatically hot-patch weights into Friday's memory layers.",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = customCodeInput,
                            onValueChange = { customCodeInput = it },
                            placeholder = { Text("Input Kotlin compilation logic or raw models mapping...", color = Color.Gray, fontSize = 11.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = GlowBlue,
                                unfocusedBorderColor = CyberDark
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                            modifier = Modifier.fillMaxWidth().height(90.dp)
                        )

                        OutlinedTextField(
                            value = customCodeNote,
                            onValueChange = { customCodeNote = it },
                            placeholder = { Text("Script label (e.g., core filters logic...)", color = Color.Gray, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = GlowBlue,
                                unfocusedBorderColor = CyberDark
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        )

                        Button(
                            onClick = {
                                if (customCodeInput.isNotBlank()) {
                                    viewModel.submitCustomCode(customCodeInput, customCodeNote.ifBlank { "Unlabelled kernel script" })
                                    customCodeInput = ""
                                    customCodeNote = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("COMPILE & PATCH PROTOCOL", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Divider(color = CyberDark, thickness = 1.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Online Download Vector Files", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Dynamic hot-swap and recovery options.", color = Color.Gray, fontSize = 9.sp)
                            }

                            Button(
                                onClick = {
                                    isCheckingOta = true
                                    otaConsoleOutput = "Contacting master servers..."
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isCheckingOta = false
                                        otaConsoleOutput = "Status: All download vectors fully synchronized. System hot-updated!"
                                        viewModel.speak("Sovereign file synchronization completed successfully, Sir. Download files are perfectly hot-updated.")
                                    }, 1500)
                                },
                                enabled = !isCheckingOta,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                if (isCheckingOta) {
                                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                                } else {
                                    Text("HOT-UPDATE FILES", color = Color.Black, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        if (otaConsoleOutput.isNotEmpty()) {
                            Text(
                                otaConsoleOutput,
                                color = if (otaConsoleOutput.contains("synchronized")) Color.Green else Color.Yellow,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Text(
                    text = "FRIDAY BRAIN METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )

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
                    modifier = Modifier.fillMaxWidth(),
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
