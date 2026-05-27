package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChatMessageEntity
import com.example.ui.FridayViewModel
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberPrimary
import com.example.ui.theme.CyberSecondary
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlowBlue
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate950
import com.example.ui.theme.WarningRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoreScreen(viewModel: FridayViewModel, paddingValues: PaddingValues) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isChatLoading.collectAsState()
    val isSpeaking by viewModel.isVoiceSpeaking.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var textInput by remember { mutableStateOf("") }
    var isVoiceModeActive by remember { mutableStateOf(false) }

    // Speech-to-Text launcher configuration
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.sendMessage(spokenText)
            }
        }
    }

    // Scroll to latest message on updates
    LaunchedEffect(chatMessages.size, isThinking) {
        if (chatMessages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(CyberDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- TOP STATUS ACCENT ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SYSTEM STATUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "FRIDAY",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Box(
                            modifier = Modifier
                                .background(CyberPrimary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .border(1.dp, CyberPrimary.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "V2.4.0",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.clearAllChatHistory() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyberCard.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, Slate700.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Format memory storage",
                            tint = WarningRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { isVoiceModeActive = !isVoiceModeActive },
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isVoiceModeActive) CyberPrimary else CyberCard.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, if (isVoiceModeActive) CyberPrimary else Slate700.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isVoiceModeActive) Icons.Default.Hearing else Icons.Default.Keyboard,
                            contentDescription = "Toggle text/vocal modality",
                            tint = if (isVoiceModeActive) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Sophisticated top active pulse widget
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberSurface)
                            .border(1.dp, Slate700, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(2.dp, CyberPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse_top")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse_top_alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .graphicsLayer(alpha = pulseAlpha)
                                    .background(CyberPrimary, CircleShape)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = CyberCard, thickness = 1.dp)

            if (isVoiceModeActive) {
                // --- VOICE MODE CONTAINER ---
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Pulsating hologram representing Friday's brain (Centered)
                        HolographicNeuroCore(
                            isThinking = isThinking,
                            isSpeaking = isSpeaking,
                            onClick = {
                                if (!isThinking) {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "বলুন Boss, আমি শুনছি...")
                                    }
                                    try {
                                        speechRecognizerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        viewModel.speak("Voice recognition is not supported on this node, Sir.")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Sophisticated Dark status subtitles below the orb
                        Text(
                            text = if (isThinking) "\"Boss, Friday is processing your request...\"" 
                                   else if (isSpeaking) "\"Sir, streaming vocal synthesis lines...\"" 
                                   else "\"Sir, শুক্রবার এখন সক্রিয়\"",
                            color = CyberPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.5.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isThinking) "COMPUTING SYSTEM NEURAL MATRICES" 
                                   else if (isSpeaking) "AUDIO WAVEFORMS EMITTING" 
                                   else "Awaiting your command...",
                            color = Color(0xFF94A3B8), // slate-400
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.SansSerif
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        if (isSpeaking) {
                            Button(
                                onClick = { viewModel.stopSpeaking() },
                                colors = ButtonDefaults.buttonColors(containerColor = WarningRed.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .border(1.dp, WarningRed.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                                    .testTag("mute_button"),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.VolumeMute, contentDescription = "Mute", tint = WarningRed)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("MUTE SYNTHESIS", color = WarningRed, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Friday, Sir...")
                                        }
                                        try {
                                            speechRecognizerLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            viewModel.speak("Voice recognition is not supported on this node, Sir.")
                                        }
                                    }
                                    .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                                    .background(CyberCard.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                                    .padding(horizontal = 24.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Trig microphone", tint = CyberPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("INITIATE SPEECH INPUT", color = CyberPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            } else {
                // --- CHAT INTERFACE ---
                Box(modifier = Modifier.weight(1f)) {
                    if (chatMessages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Subtly blurred cyan sphere background drawing
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .background(CyberSecondary.copy(alpha = 0.05f), CircleShape)
                                    .blur(60.dp)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = "SYSTEM GATEWAY ACTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Send a direct console directive to start neural synchronization.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8), // slate-400
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(chatMessages) { message ->
                                ChatBubbleItem(message = message)
                            }
                            if (isThinking) {
                                item {
                                    ThinkingPlaceholder()
                                }
                            }
                        }
                    }
                }

                Divider(color = CyberCard, thickness = 1.dp)

                // Input bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CyberSurface
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .navigationBarsPadding()
                            .imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Compile directive to Friday...", color = Color.Gray, fontSize = 13.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, CyberCard, RoundedCornerShape(20.dp))
                                .testTag("chat_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = CyberCard.copy(alpha = 0.6f),
                                unfocusedContainerColor = CyberCard.copy(alpha = 0.4f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendMessage(textInput)
                                    textInput = ""
                                }
                            },
                            containerColor = CyberPrimary,
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("send_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send directive", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HolographicNeuroCore(
    isThinking: Boolean,
    isSpeaking: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")

    // Pulse animation (Scalar dimensions)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isThinking) 600 else if (isSpeaking) 900 else 1800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Rotation degrees
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isThinking) 2000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val coreGlowColor = if (isThinking) GlowBlue else if (isSpeaking) WarningRed else CyberPrimary

    Box(
        modifier = Modifier
            .size(240.dp)
            .graphicsLayer(
                scaleX = pulseScale,
                scaleY = pulseScale,
                rotationZ = rotationAngle
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Blur background glow (Atmosphere)
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(coreGlowColor.copy(alpha = 0.15f), CircleShape)
                .blur(30.dp)
        )

        // Outer Layer: w-44 rounded-full bg-gradient-to-tr from-slate-900 via-slate-800 to-slate-900 border border-slate-700
        Box(
            modifier = Modifier
                .size(176.dp) // 176.dp equals 44.dp * 4 (w-44)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CyberCard,       // Inside slate-800
                            CyberSurface,    // Middle slate-900
                            CyberDark        // Outer pure black
                        )
                    )
                )
                .border(1.dp, Slate700, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Middle Layer: w-32 rounded-full border border-cyan-500/40
            Box(
                modifier = Modifier
                    .size(128.dp) // 128.dp equals 32.dp * 4 (w-32)
                    .border(1.dp, coreGlowColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Inner Layer: w-24 rounded-full border-2 border-cyan-400 bg-slate-950
                Box(
                    modifier = Modifier
                        .size(96.dp) // 96.dp equals 24.dp * 4 (w-24)
                        .clip(CircleShape)
                        .background(Slate950)
                        .border(2.dp, coreGlowColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.VolumeUp else if (isThinking) Icons.Default.Hearing else Icons.Default.Mic,
                        contentDescription = "Friday Core Voice Module",
                        tint = coreGlowColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessageEntity) {
    val isCreator = message.sender == "creator"
    val bubbleColor = if (isCreator) CyberSecondary.copy(alpha = 0.15f) else CyberCard
    val borderCol = if (isCreator) CyberSecondary.copy(alpha = 0.5f) else CyberPrimary.copy(alpha = 0.3f)
    val alignment = if (isCreator) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (isCreator) Alignment.End else Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isCreator) "BOSS / SIR" else "FRIDAY v3.5",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCreator) CyberSecondary else CyberPrimary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isCreator) 16.dp else 2.dp,
                    bottomEnd = if (isCreator) 2.dp else 16.dp
                ),
                border = BorderStroke(1.dp, borderCol),
                modifier = Modifier.testTag("chat_bubble_${message.id}")
            ) {
                Text(
                    text = message.messageText,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(14.dp),
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
fun ThinkingPlaceholder() {
    val infiniteTransition = rememberInfiniteTransition(label = "think")
    val alphaVal by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .background(CyberCard, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp))
                .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .graphicsLayer(alpha = alphaVal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Friday is analyzing neural inputs...",
                color = CyberPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
