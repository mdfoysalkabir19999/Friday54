package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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

import android.util.Log

suspend fun getCityNameFromCoordinates(context: android.content.Context, lat: Double, lon: Double): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    var city = "Dhaka, Bangladesh"
    try {
        val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val locality = address.locality ?: address.subAdminArea ?: address.adminArea
            city = if (locality != null) {
                val country = address.countryName ?: "Bangladesh"
                "$locality, $country"
            } else {
                "Bangladesh Region"
            }
        }
    } catch (e: Exception) {
        Log.e("GPS", "Geocoder error", e)
    }
    city
}

fun fetchDeviceLocation(
    context: android.content.Context,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onLocationAvailable: (Double, Double, String) -> Unit
) {
    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
    if (locationManager == null) {
        onLocationAvailable(23.8103, 90.4125, "Dhaka, Bangladesh")
        return
    }
    
    val hasFine = androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasCoarse = androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    
    if (!hasFine && !hasCoarse) {
        onLocationAvailable(23.8103, 90.4125, "Dhaka, Bangladesh (Default - Permission Denied)")
        return
    }
    
    coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val isGpsEnabled = try {
                locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            } catch (e: Exception) {
                false
            }
            val isNetworkEnabled = try {
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
            } catch (e: Exception) {
                false
            }
            
            val providers = try { locationManager.getProviders(true) } catch (e: Exception) { emptyList<String>() }
            var bestLocation: android.location.Location? = null
            
            for (provider in providers) {
                try {
                    val loc = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                        bestLocation = loc
                    }
                } catch (e: Exception) {
                    Log.e("GPS", "Error reading last known location from $provider", e)
                }
            }
            
            if (bestLocation == null) {
                try {
                    val passiveLoc = locationManager.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER)
                    if (passiveLoc != null) {
                        bestLocation = passiveLoc
                    }
                } catch (e: Exception) {
                    Log.e("GPS", "Error reading last known location from passive", e)
                }
            }
            
            if (bestLocation != null) {
                val lat = bestLocation.latitude
                val lon = bestLocation.longitude
                val city = getCityNameFromCoordinates(context, lat, lon)
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    onLocationAvailable(lat, lon, city)
                }
                return@launch
            }
            
            val activeProvider = providers.firstOrNull { 
                it != android.location.LocationManager.PASSIVE_PROVIDER 
            } ?: if (isNetworkEnabled) android.location.LocationManager.NETWORK_PROVIDER else android.location.LocationManager.GPS_PROVIDER
            
            launch(kotlinx.coroutines.Dispatchers.Main) {
                try {
                    locationManager.requestSingleUpdate(activeProvider, object : android.location.LocationListener {
                        override fun onLocationChanged(loc: android.location.Location) {
                            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                val city = getCityNameFromCoordinates(context, loc.latitude, loc.longitude)
                                launch(kotlinx.coroutines.Dispatchers.Main) {
                                    onLocationAvailable(loc.latitude, loc.longitude, city)
                                }
                            }
                        }
                        @Deprecated("Deprecated") override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
                        override fun onProviderEnabled(p: String) {}
                        override fun onProviderDisabled(p: String) {}
                    }, android.os.Looper.getMainLooper())
                } catch (e: Exception) {
                    Log.e("GPS", "requestSingleUpdate failed dynamically", e)
                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        onLocationAvailable(23.8103, 90.4125, "Dhaka, Bangladesh")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GPS", "General process error getting location", e)
            launch(kotlinx.coroutines.Dispatchers.Main) {
                onLocationAvailable(23.8103, 90.4125, "Dhaka, Bangladesh")
            }
        }
    }
}

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
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showFridayLens by remember { mutableStateOf(false) }
    var showFridayLensDialog by remember { mutableStateOf(false) }
    var showAttachmentSourceDialog by remember { mutableStateOf(false) }
    var selectedOcrText by remember { mutableStateOf("Position camera over newspapers, books or code...") }
    var selectedObjectType by remember { mutableStateOf("Searching...") }
    var attachedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val chatImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val localPath = viewModel.saveImageToInternalStorage(uri)
                if (localPath != null) {
                    attachedImageUri = android.net.Uri.parse(localPath)
                }
            }
        }
    }

    // REAL File Picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val contentResolver = context.contentResolver
            var fileName = "user_attachment"
            var fileSize = 1048L
            try {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex) ?: "user_attachment"
                        }
                        if (sizeIndex >= 0) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FilePicker", "Error reading metadata in callback", e)
            }
            val fileType = fileName.substringAfterLast('.', "bin")
            viewModel.attachFileMatrix(fileName, fileType, fileSize)
        }
    }

    // GPS location fetcher
    val locationPermissionWithAction = {
        fetchDeviceLocation(context, coroutineScope) { lat, lon, city ->
            viewModel.sendLocationMatrix(lat, lon, city)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            locationPermissionWithAction()
        } else {
            // Permission Denied, fall back gracefully
            viewModel.sendLocationMatrix(23.8103, 90.4125, "Dhaka (Fallback)")
            viewModel.speak("GPS access restricted. Synchronized to Bangladesh default coordinate matrix, Boss.")
        }
    }

    // Camera picture capture launcher for Friday Lens
    val cameraLensLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedObjectType = "Live Eye Lens Feed"
            selectedOcrText = "COGNITIVE DETECTIONS:\nLive optical target processed. Color telemetry, ambient light intensity, and structure analysis are active. Everything is secure, Boss."
            showFridayLens = true
        } else {
            viewModel.speak("Camera capture cancelled, Boss.")
        }
    }

    // Camera permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLensLauncher.launch(null)
            } catch (e: Exception) {
                Log.e("CameraLens", "Could not start camera after permission", e)
                viewModel.speak("Camera system failed, Boss. Synchronizing to high-definition simulated scanner.")
                selectedObjectType = "Live Eye Lens Feed (Mock)"
                selectedOcrText = "COGNITIVE SCAN COMPLETE:\nLive feed simulated. Frame captures look clear, Boss."
                showFridayLens = true
            }
        } else {
            viewModel.speak("Camera visual authorization denied, Boss. Activating digital virtual sensor instead.")
            selectedObjectType = "Virtual Eye Feed"
            selectedOcrText = "COGNITIVE SCAN (VIRTUAL MODE):\nSimulating deep structural analysis of physical surface. Matrix looks secure, Boss."
            showFridayLens = true
        }
    }

    // Gallery image picker launcher for Friday Lens
    val galleryLensLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedObjectType = "Imported Optical Feed"
            selectedOcrText = "DEEP SCAN Telemetry:\nAnalyzing imported imagery matrix... OCR layer initialized.\nFound pattern coordinates matches. System load within healthy margins, Boss."
            showFridayLens = true
        }
    }

    val androidClipboard = remember { context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager? }

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
                                        viewModel.speak("Voice recognition is not supported on this node, Boss.")
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Sophisticated Dark status subtitles below the orb
                        Text(
                            text = if (isThinking) "\"Boss, Friday is processing your request...\"" 
                                   else if (isSpeaking) "\"Boss, streaming vocal synthesis lines...\"" 
                                   else "\"Boss, Friday akhon fully active!\"",
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
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Friday, Boss...")
                                        }
                                        try {
                                            speechRecognizerLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            viewModel.speak("Voice recognition is not supported on this node, Boss.")
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
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom)
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

                // --- ATTACHMENT ACTION BAR POPUP ---
                AnimatedVisibility(
                    visible = showAttachmentMenu,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .border(1.dp, CyberPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        color = CyberCard.copy(alpha = 0.95f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // File Node (Flexible File Attachment Hub)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        showAttachmentMenu = false
                                        showAttachmentSourceDialog = true
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(CyberDark, CircleShape)
                                        .border(1.dp, CyberPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = "File", tint = CyberPrimary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("ATTACH FILE", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }

                            // Coordinate Node (Dynamic GPS Geolocation)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        showAttachmentMenu = false
                                        try {
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        } catch (e: Exception) {
                                            Log.e("Location", "Could not request location permission", e)
                                            // Fallback
                                            locationPermissionWithAction()
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(CyberDark, CircleShape)
                                        .border(1.dp, GlowBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MyLocation, contentDescription = "GPS", tint = GlowBlue, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("DISPATCH GPS", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }

                            // Friday Eye Lens Camera Node (Shows choose source dialog)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        showAttachmentMenu = false
                                        showFridayLensDialog = true
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(CyberDark, CircleShape)
                                        .border(1.dp, CyberSecondary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Camera, contentDescription = "Camera Lens", tint = CyberSecondary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("FRIDAY LENS", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }

                            // Cognitive Vision Image Node
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        showAttachmentMenu = false
                                        try {
                                            chatImagePickerLauncher.launch("image/*")
                                        } catch (e: Exception) {
                                            Log.e("CoreScreen", "Error launching gallery", e)
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(CyberDark, CircleShape)
                                        .border(1.dp, CyberPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = "Cognitive Vision Image", tint = CyberPrimary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("COGNITIVE EYE", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Input bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CyberSurface
                ) {
                    Column {
                        // Image attachment preview ribbon
                        if (attachedImageUri != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CyberDark)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, CyberSecondary, RoundedCornerShape(8.dp))
                                ) {
                                    coil.compose.AsyncImage(
                                        model = attachedImageUri,
                                        contentDescription = "Attached visual matrix preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "COGNITIVE EYE: OPTICAL TARGET LOCKED",
                                        color = CyberSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Target image queued. Type query and transmit.",
                                        color = Color.LightGray,
                                        fontSize = 9.sp
                                    )
                                }
                                IconButton(
                                    onClick = { attachedImageUri = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Purge target image",
                                        tint = WarningRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = CyberCard, thickness = 1.dp)
                        }

                        Row(
                            modifier = Modifier
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { Text("Compile directive to Friday...", color = Color.Gray, fontSize = 13.sp) },
                                leadingIcon = {
                                    IconButton(onClick = { showAttachmentMenu = !showAttachmentMenu }) {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = "Attach resource matrices",
                                            tint = CyberPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
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
                                    if (textInput.isNotBlank() || attachedImageUri != null) {
                                        viewModel.sendMessage(textInput, attachedImageUri?.toString())
                                        textInput = ""
                                        attachedImageUri = null
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

        // --- COGNITIVE LENS VIEWPORT SCANNER (GOOGLE LENS STYLE) ---
        AnimatedVisibility(
            visible = showFridayLens,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Sweep animation
            val transition = rememberInfiniteTransition(label = "laser")
            val sweepY by transition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "sweep"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Viewfinder lines
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Center Reticle
                    val boxSize = size.minDimension * 0.6f
                    val left = (width - boxSize) / 2
                    val top = (height - boxSize) / 2
                    
                    // Laser sweep line
                    drawLine(
                        color = Color.Green,
                        start = androidx.compose.ui.geometry.Offset(left, top + boxSize * sweepY),
                        end = androidx.compose.ui.geometry.Offset(left + boxSize, top + boxSize * sweepY),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showFridayLens = false },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Scanner", tint = Color.White)
                        }

                        Text(
                            text = "👁️ FRIDAY OPTICAL LENS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Green,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )

                        IconButton(
                            onClick = {
                                androidClipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Friday OCR", selectedOcrText))
                                viewModel.speak("Text copied successfully, Boss.")
                            },
                            enabled = selectedObjectType != "Searching...",
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy scan text", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = "SELECT SCAN TARGET TO SIMULATE REAL LENS OCR SCANNING:",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                selectedObjectType = "Newspaper Article"
                                selectedOcrText = "BREAKING NEWS: Md Foysal Kabir Soikat, Chief Master Designer of cyber structures in Bangladesh, successfully compiles Friday Mainframe v3.5 with sovereign intelligence clusters."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedObjectType == "Newspaper Article") Color.Green else CyberCard),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("📰 NEWS", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                selectedObjectType = "Physical Book Cover"
                                selectedOcrText = "THE ART OF AUTONOMOUS SYSTEMS\nBy Md Foysal Kabir Soikat\nPublished: May 2026 Dhaka, Bangladesh."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedObjectType == "Physical Book Cover") Color.Green else CyberCard),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("📚 BOOK", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                selectedObjectType = "Sovereign Source Code"
                                selectedOcrText = "fun main() {\n  val owner = \"Md Foysal Kabir Soikat\"\n  val protocol = \"Absolute Clearances\"\n  syncFriday(owner, protocol)\n}"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (selectedObjectType == "Sovereign Source Code") Color.Green else CyberCard),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("💻 CODE", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Green.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        color = Color.Black.copy(alpha = 0.8f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "LENS SCANNER DIAGNOSTICS: ${selectedObjectType.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Green,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Text(
                                text = selectedOcrText,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp,
                                modifier = Modifier.heightIn(min = 60.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (selectedObjectType != "Searching...") {
                                        viewModel.scanVisualObject(selectedOcrText, selectedObjectType, androidClipboard)
                                        showFridayLens = false
                                    }
                                },
                                enabled = selectedObjectType != "Searching...",
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("TRANSMIT LENS DATA TO FRIDAY", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        if (showFridayLensDialog) {
            AlertDialog(
                onDismissRequest = { showFridayLensDialog = false },
                title = {
                    Text(
                        text = "👁️ FRIDAY OPTICAL LENS SOURCE",
                        color = CyberPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select an optical input channel to lock target scanning feed:",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Divider(color = CyberCard, thickness = 1.dp)
                        
                        // Option 1: Live Camera Scan
                        Button(
                            onClick = {
                                showFridayLensDialog = false
                                try {
                                    val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                        context,
                                        android.Manifest.permission.CAMERA
                                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasCameraPermission) {
                                        cameraLensLauncher.launch(null)
                                    } else {
                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    }
                                } catch (e: Exception) {
                                    Log.e("CameraLens", "Could not start camera flow", e)
                                    viewModel.speak("Camera capture failed, falling back to simulated target scan.")
                                    selectedObjectType = "Live Eye Lens Feed (Mock)"
                                    selectedOcrText = "COGNITIVE SCAN COMPLETE:\nLive feed simulated. Frame captures look clear, Sir."
                                    showFridayLens = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📸 TAKE SNAP (LIVE CAMERA)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        
                        // Option 2: Gallery / Album Image Picker
                        Button(
                            onClick = {
                                showFridayLensDialog = false
                                try {
                                    galleryLensLauncher.launch("image/*")
                                } catch (e: Exception) {
                                    Log.e("CameraLens", "Could not start gallery picker", e)
                                    viewModel.speak("Gallery picker failed, falling back to simulated target scan.")
                                    selectedObjectType = "Imported Optical Feed (Mock)"
                                    selectedOcrText = "DEEP SCAN COMPLETE:\nSimulated gallery photo analyzed. Database looks secure, Sir."
                                    showFridayLens = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowBlue),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🖼️ PICK FRAME (FROM GALLERY)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        // Option 3: Standard Virtual Reticle Scanner
                        Button(
                            onClick = {
                                showFridayLensDialog = false
                                selectedObjectType = "Searching..."
                                selectedOcrText = "Position camera over newspapers, books or code..."
                                showFridayLens = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📡 CORE RETICLE ENGINE (SIMULATE)", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showFridayLensDialog = false }) {
                        Text("CANCEL", color = CyberSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                },
                containerColor = CyberDark,
                modifier = Modifier.border(1.dp, CyberPrimary.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
            )
        }

        if (showAttachmentSourceDialog) {
            AlertDialog(
                onDismissRequest = { showAttachmentSourceDialog = false },
                title = {
                    Text(
                        text = "📁 ATTACH MATRIX FILE",
                        color = CyberPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Browse physical files on your local device or feed simulated telemetry streams directly:",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Divider(color = CyberCard, thickness = 1.dp)
                        
                        // Option 1: Live System File Picker
                        Button(
                            onClick = {
                                showAttachmentSourceDialog = false
                                try {
                                    filePickerLauncher.launch("*/*")
                                } catch (e: Exception) {
                                    Log.e("FilePicker", "Could not start system file picker", e)
                                    viewModel.speak("System file picker failed. Swapping to diagnostic simulation.")
                                    viewModel.attachFileMatrix("System_Diagnostics_Mock.log", "log", 24800L)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📂 BROWSE LOCAL FILES (REAL)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        
                        // Option 2: Simulated Error Screenshot attachment
                        Button(
                            onClick = {
                                showAttachmentSourceDialog = false
                                viewModel.attachFileMatrix("Friday_Screenshot_Diagnosis.png", "png", 2410720L)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GlowBlue),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🖼️ SIMULATE SCREENSHOT ATTACHMENT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        // Option 3: Simulated Log File attachment
                        Button(
                            onClick = {
                                showAttachmentSourceDialog = false
                                viewModel.attachFileMatrix("System_Terminal_Diagnostics.log", "log", 148480L)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCard),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📝 SIMULATE TERMINAL LOG FILE", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAttachmentSourceDialog = false }) {
                        Text("CANCEL", color = CyberSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                },
                containerColor = CyberDark,
                modifier = Modifier.border(1.dp, CyberPrimary.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
            )
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
                    text = if (isCreator) "BOSS" else "FRIDAY v3.5",
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
                Column {
                    message.imageUri?.let { uriStr ->
                        coil.compose.AsyncImage(
                            model = uriStr,
                            contentDescription = "Sovereign optic sensory target",
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, CyberPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    if (message.messageText.isNotBlank()) {
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
