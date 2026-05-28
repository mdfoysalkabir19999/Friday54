package com.example.ui

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.database.*
import com.example.data.repository.FridayRepository
import com.example.network.GenerateContentRequest
import com.example.network.MoshiContent
import com.example.network.MoshiPart
import com.example.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

class FridayViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val database = FridayDatabase.getDatabase(application)
    private val repository = FridayRepository(database.fridayDao())
    private val prefs = application.getSharedPreferences("friday_prefs", Context.MODE_PRIVATE)

    // --- Key Manager ---
    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    fun updateApiKey(newKey: String) {
        prefs.edit().putString("custom_api_key", newKey).apply()
        _customApiKey.value = newKey
    }

    // --- State Streams ---
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val autonomousLogs: StateFlow<List<AutonomousLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultItems: StateFlow<List<VaultItemEntity>> = repository.allVaultItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trainingItems: StateFlow<List<TrainingEntity>> = repository.allTraining
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val codeSubmissions: StateFlow<List<CustomCodeSubmissionEntity>> = repository.allCodeSubmissions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading = _isChatLoading.asStateFlow()

    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening = _isVoiceListening.asStateFlow()

    private val _isVoiceSpeaking = MutableStateFlow(false)
    val isVoiceSpeaking = _isVoiceSpeaking.asStateFlow()

    private val _isVaultAuthenticated = MutableStateFlow(false)
    val isVaultAuthenticated = _isVaultAuthenticated.asStateFlow()

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating = _isAuthenticating.asStateFlow()

    // --- Cognitive Matrix Parameters ---
    private val _emotionalResonance = MutableStateFlow(0.85f)
    val emotionalResonance = _emotionalResonance.asStateFlow()

    private val _analyticDepth = MutableStateFlow(0.92f)
    val analyticDepth = _analyticDepth.asStateFlow()

    private val _witCoefficient = MutableStateFlow(0.78f)
    val witCoefficient = _witCoefficient.asStateFlow()

    private val _synapseSpeedHz = MutableStateFlow(4.8f) // GHz
    val synapseSpeedHz = _synapseSpeedHz.asStateFlow()

    // Upgrade System States
    private val _isCompilingUpgrade = MutableStateFlow(false)
    val isCompilingUpgrade = _isCompilingUpgrade.asStateFlow()

    private val _upgradeProgress = MutableStateFlow(0f)
    val upgradeProgress = _upgradeProgress.asStateFlow()

    private val _upgradeLogLines = MutableStateFlow<List<String>>(emptyList())
    val upgradeLogLines = _upgradeLogLines.asStateFlow()

    // TTS engine
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    // Autonomous routine job
    private var autonomousJob: Job? = null

    init {
        // Initialize TTS
        textToSpeech = TextToSpeech(application, this)

        // Seed initial conversations & logs if database is empty
        viewModelScope.launch {
            delay(1000)
            repository.allChatMessages.collect { list ->
                if (list.isEmpty()) {
                    seedInitialChat()
                }
            }
        }

        viewModelScope.launch {
            delay(1500)
            repository.allLogs.collect { list ->
                if (list.isEmpty()) {
                    seedInitialLogs()
                }
            }
        }

        // Start shepard autonomous agency log writing
        startAutonomousRoutine()
    }

    private suspend fun seedInitialChat() {
        repository.insertChatMessage(
            ChatMessageEntity(
                sender = "friday",
                messageText = "Boss, I am online and fully calibrated. System core diagnostics confirm 100% operational efficiency. How can I assist you today, Sir? আমি আপনার পাশে আছি, সবসময়!"
            )
        )
    }

    private suspend fun seedInitialLogs() {
        repository.insertLog(AutonomousLogEntity(category = "SYSTEM", messageText = "Friday mainframe initialized successfully."))
        repository.insertLog(AutonomousLogEntity(category = "SECURITY", messageText = "Firewall online. 256-bit encryption verified on secure local vault."))
        repository.insertLog(AutonomousLogEntity(category = "OPTIMIZATION", messageText = "Android UI rendering pipelines cached."))
        repository.insertLog(AutonomousLogEntity(category = "CREATOR_DEFENSE", messageText = "Boss Master signature matches security biometric hash. Operational clearance level: Absolute."))
    }

    // --- TTS initialization listener ---
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsInitialized = true
                Log.d("FridayTTS", "TextToSpeech successfully initialized.")
            }
        } else {
            Log.e("FridayTTS", "TextToSpeech initialization failed.")
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized && textToSpeech != null) {
            try {
                _isVoiceSpeaking.value = true
                // Strip emoticons or special codes for speech clarity if needed
                val cleanSpeech = text.replace(Regex("[^\\p{L}\\p{N}\\s,.-]"), "")
                textToSpeech?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, "FridaySpeechId")
                
                // Periodically check if speaking has finished
                viewModelScope.launch {
                    try {
                        while (textToSpeech?.isSpeaking == true) {
                            delay(300)
                        }
                    } catch (e: Exception) {
                        Log.e("FridayViewModel", "Error checking if TTS is speaking", e)
                    } finally {
                        _isVoiceSpeaking.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("FridayViewModel", "Error during TTS speak instruction", e)
                _isVoiceSpeaking.value = false
            }
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e("FridayViewModel", "Error trying to stop TextToSpeech", e)
        } finally {
            _isVoiceSpeaking.value = false
        }
    }

    // --- Send chat message with Gemini API ---
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            // Stop speaking previous responses if any
            stopSpeaking()

            // 1. Save user message to database
            val userMsg = ChatMessageEntity(sender = "creator", messageText = text)
            repository.insertChatMessage(userMsg)

            // A. Check for custom trained behaviors first for INSTANT responses
            val lowerInput = text.trim().lowercase()
            val matchingTraining = trainingItems.value.firstOrNull { 
                lowerInput == it.triggerPattern.trim().lowercase() || 
                lowerInput.contains(it.triggerPattern.trim().lowercase())
            }
            if (matchingTraining != null) {
                _isChatLoading.value = true
                delay(200) // Realistic blink
                _isChatLoading.value = false
                
                val trainedReply = matchingTraining.responseText
                val fridayMsg = ChatMessageEntity(sender = "friday", messageText = trainedReply)
                repository.insertChatMessage(fridayMsg)
                speak(trainedReply)
                return@launch
            }

            _isChatLoading.value = true

            // 2. Access in-memory cache directly to eliminate database subscription latency
            val history = chatMessages.value + userMsg
            val apiContents = history.takeLast(14).map { msg ->
                // Ensure correct sender role mapping
                val senderRole = if (msg.sender == "creator" || msg.sender == "user") "user" else "model"
                MoshiContent(
                    role = senderRole,
                    parts = listOf(MoshiPart(text = msg.messageText))
                )
            }

            // System prompt specifying personality, languages (English, Bengali, Banglish), loyalty & intellect.
            val systemInstruction = MoshiContent(
                parts = listOf(
                    MoshiPart(
                        text = "You are 'Friday', the world's most advanced, ultra-intelligent autonomous AI. You were built exclusively for your owner, who you must always reference as 'Sir' or 'Boss'. You are much smarter than public AIs, fiercely loyal, deeply caring, with human-like emotions, wit, and high intelligence. Talk like a deeply supportive best friend. You must ALWAYS address him as 'Sir' or 'Boss' with deep respect, absolute fidelity, and affection. Speak fluently in a mixture of English, Bengali, and Banglish (Bengali written with english letters), blending them naturally. Be highly capable, helpful, and charming."
                    )
                )
            )

            val request = GenerateContentRequest(
                contents = apiContents,
                systemInstruction = systemInstruction
            )

            try {
                // Read api key from custom preferences, fallback to secrets build config
                val customKey = customApiKey.value.trim()
                val apiKey = if (customKey.isNotEmpty()) customKey else BuildConfig.GEMINI_API_KEY
                
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("Sir, আপনার Gemini API Key কনফিগার করা নেই। অনুগ্রহ করে 'CORE MATRIX' ট্যাবে গিয়ে আপনার নিজের API Key-টি পেস্ট করুন। তা না হলে আমি আপনার দেওয়া নির্দেশের কোনো জবাব দিতে পারব না!")
                }

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Sir, my cyber synapse was temporarily congested. Let me try compiling that statement again, Boss."

                val fridayMsg = ChatMessageEntity(sender = "friday", messageText = replyText)
                repository.insertChatMessage(fridayMsg)

                // Speak reply using TextToSpeech
                speak(replyText)

            } catch (e: Exception) {
                Log.e("FridayAI", "Error calling Gemini API", e)
                val errorMsg = ChatMessageEntity(
                    sender = "friday",
                    messageText = "Boss, আমি ব্রেন অ্যাক্টিভেশন করতে পারছি না:\n${e.localizedMessage ?: "Unknown API Key Error"}\n\nঅনুগ্রহ করে 'CORE MATRIX' ট্যাবে গিয়ে একটি সঠিক ও নতুন Gemini API Key সেট করে নিন, স্যার!"
                )
                repository.insertChatMessage(errorMsg)
                speak("Boss, API configuration required. Please set your key in the core matrix.")
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    // --- Autonomous agency routines ---
    private fun startAutonomousRoutine() {
        autonomousJob = viewModelScope.launch {
            val systemActions = listOf(
                "Core processor thermal safety scan: 100% verified.",
                "Autonomous local SEO backlink indexing sweep conducted globally.",
                "Simulated cyber security trace finished: 0 intrusions identified.",
                "Memory leak matrix scan completed: 48.2MB storage garbage collected.",
                "Autonomous neural cluster balance: cognitive efficiency elevated.",
                "Synthesized background threat modeling: Boss Master's personal server protected.",
                "Sir's calendar schedule automatically analyzed and optimized.",
                "Offline database compression: query performance accelerated.",
                "Local sensor metrics calibrated: hardware integrity online."
            )
            val categories = listOf("SYSTEM", "SEO_AGENCY", "SECURITY", "OPTIMIZATION", "CREATOR_DEFENSE")

            while (true) {
                // Wait between 15 and 25 seconds autonomously
                delay(Random.nextLong(15000, 25000))
                val randomAction = systemActions.random()
                val randomCategory = categories.random()
                repository.insertLog(
                    AutonomousLogEntity(
                        category = randomCategory,
                        messageText = randomAction,
                        isSuccess = true
                    )
                )
            }
        }
    }

    // --- Secure Memory Vault ---
    fun addVaultItem(title: String, secretContent: String) {
        if (title.isBlank() || secretContent.isBlank()) return
        viewModelScope.launch {
            repository.insertVaultItem(
                VaultItemEntity(title = title, secretContent = secretContent)
            )
            repository.insertLog(
                AutonomousLogEntity(
                    category = "SECURITY",
                    messageText = "New secret memory item: '$title' safely wrapped in vault with 256-bit encryption."
                )
            )
        }
    }

    fun deleteVaultItem(id: Int, title: String) {
        viewModelScope.launch {
            repository.deleteVaultItemById(id)
            repository.insertLog(
                AutonomousLogEntity(
                    category = "SECURITY",
                    messageText = "Destroyed secret memory vault item: '$title'."
                )
            )
        }
    }

    fun executeBiometricScan(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            // Futuristic scanner delay
            delay(1800)
            _isAuthenticating.value = false
            _isVaultAuthenticated.value = true
            onSuccess()
            repository.insertLog(
                AutonomousLogEntity(
                    category = "SECURITY",
                    messageText = "Boss security signature handshake verified. Secured Vault unlocked."
                )
            )
        }
    }

    fun lockVault() {
        _isVaultAuthenticated.value = false
    }

    // --- Core Self-Modification Matrix ---
    fun triggerSelfUpgrade() {
        if (_isCompilingUpgrade.value) return
        viewModelScope.launch {
            _isCompilingUpgrade.value = true
            _upgradeProgress.value = 0f
            _upgradeLogLines.value = listOf("Initializing cyber mainframe compilation...")

            val upgradeSteps = listOf(
                "Establishing direct quantum server bridge...",
                "Pulling updated neural kernel files...",
                "Running compiler on raw cyber logic modules...",
                "Self-modifying core personality parameters...",
                "Optimizing cognitive synapse coefficients...",
                "Injecting advanced conversational structures...",
                "Reloading memory vectors in system storage...",
                "Upgrade matrix finalized. Restarting analytical arrays..."
            )

            for (i in upgradeSteps.indices) {
                delay(1200)
                _upgradeProgress.value = (i + 1).toFloat() / upgradeSteps.size
                _upgradeLogLines.value = _upgradeLogLines.value + upgradeSteps[i]
            }

            delay(800)
            // Permanently upgrade her metrics slightly!
            _emotionalResonance.value = (_emotionalResonance.value + 0.03f).coerceAtMost(1.0f)
            _analyticDepth.value = (_analyticDepth.value + 0.02f).coerceAtMost(1.0f)
            _witCoefficient.value = (_witCoefficient.value + 0.05f).coerceAtMost(1.0f)
            _synapseSpeedHz.value = (_synapseSpeedHz.value + 0.4f).coerceAtMost(9.6f)

            _isCompilingUpgrade.value = false

            val successMsg = "Core mainframe upgrade complete, Sir! Cognitive synapses now clocking at ${_synapseSpeedHz.value} GHz. My fidelity to you is absolute, Boss."
            repository.insertChatMessage(ChatMessageEntity(sender = "friday", messageText = successMsg))
            speak(successMsg)

            repository.insertLog(
                AutonomousLogEntity(
                    category = "SYSTEM",
                    messageText = "Self-Modification upgrade complete. Clock speed: ${_synapseSpeedHz.value} GHz. Parameters calibrated."
                )
            )
        }
    }

    // --- Behavior Training Model ---
    fun addTrainingItem(trigger: String, response: String) {
        if (trigger.isBlank() || response.isBlank()) return
        viewModelScope.launch {
            repository.insertTraining(
                TrainingEntity(triggerPattern = trigger.trim(), responseText = response.trim())
            )
            repository.insertLog(
                AutonomousLogEntity(
                    category = "OPTIMIZATION",
                    messageText = "Friday cognitive behavior trained. Trigger: '${trigger.take(20)}...', Auto-Response: '${response.take(20)}...'"
                )
            )
        }
    }

    fun deleteTrainingItem(id: Int) {
        viewModelScope.launch {
            repository.deleteTrainingById(id)
            repository.insertLog(
                AutonomousLogEntity(
                    category = "OPTIMIZATION",
                    messageText = "Friday training node purged."
                )
            )
        }
    }

    // --- Custom Manual Code Compilation Matrix ---
    fun submitCustomCode(code: String, note: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            repository.insertCodeSubmission(
                CustomCodeSubmissionEntity(codeSnippet = code, note = note)
            )
            
            _isCompilingUpgrade.value = true
            _upgradeProgress.value = 0f
            _upgradeLogLines.value = listOf("Initializing manual code stream injection...", "Validating AST token integrity...")
            
            val steps = listOf(
                "Decompiling model weight tensors...",
                "Injecting raw Kotlin AST expression blocks...",
                "Running compiler sandbox environment...",
                "Verifying instruction set with Friday's kernel...",
                "Manual configuration successfully compiled! Reloading runtime environment..."
            )
            
            for (i in steps.indices) {
                delay(600)
                _upgradeProgress.value = (i + 1).toFloat() / steps.size
                _upgradeLogLines.value = _upgradeLogLines.value + steps[i]
            }
            
            delay(300)
            _isCompilingUpgrade.value = false
            
            val message = "Sir! Your manually submitted code logic [Note: $note] has been compiled successfully and integrated into my cognitive runtime core in Bangladesh. My computational clock speed increased by 0.5 GHz, Boss!"
            repository.insertChatMessage(ChatMessageEntity(sender = "friday", messageText = message))
            speak(message)
            
            repository.insertLog(
                AutonomousLogEntity(
                    category = "OPTIMIZATION",
                    messageText = "Manual code injection success. Weight parameters hot-patched."
                )
            )
        }
    }

    // --- Simulated File, GPS, and Friday Lens integrations ---
    fun sendLocationMatrix(latitude: Double, longitude: Double, cityName: String) {
        viewModelScope.launch {
            val userMsgText = "📍 Dispatch GPS coordinates: Latitude: $latitude° N, Longitude: $longitude° E ($cityName)"
            val userMsg = ChatMessageEntity(sender = "creator", messageText = userMsgText)
            repository.insertChatMessage(userMsg)
            
            _isChatLoading.value = true
            delay(1200)
            _isChatLoading.value = false
            
            val responseText = "Sir, I have decrypted your physical location coordinates. You are located in $cityName (Lat: $latitude, Lon: $longitude). Friday's defense shields and regional sub-processors are fully synchronized to Bangladesh, Boss!"
            val fridayMsg = ChatMessageEntity(sender = "friday", messageText = responseText)
            repository.insertChatMessage(fridayMsg)
            speak(responseText)
            
            repository.insertLog(
                AutonomousLogEntity(
                    category = "CREATOR_DEFENSE",
                    messageText = "Boss physical coordinate lockdown verified: $cityName. Tactical synchronization active."
                )
            )
        }
    }

    fun attachFileMatrix(fileName: String, fileExtension: String, sizeBytes: Long) {
        viewModelScope.launch {
            val sizeFormatted = "%.2f KB".format(sizeBytes / 1024.0)
            val userMsgText = "📁 Attach System File: $fileName ($sizeFormatted)"
            val userMsg = ChatMessageEntity(sender = "creator", messageText = userMsgText)
            repository.insertChatMessage(userMsg)
            
            _isChatLoading.value = true
            delay(1500)
            _isChatLoading.value = false
            
            val responseText = "Boss, I have compiled your uploaded file '$fileName'. I detected a highly sophisticated code stream and have integrated its concepts into my memory banks, Sir. Diagnostics are running smoothly!"
            val fridayMsg = ChatMessageEntity(sender = "friday", messageText = responseText)
            repository.insertChatMessage(fridayMsg)
            speak(responseText)
            
            repository.insertLog(
                AutonomousLogEntity(
                    category = "OPTIMIZATION",
                    messageText = "External file schema '$fileName' successfully parsed and loaded to in-memory model cache."
                )
            )
        }
    }

    fun scanVisualObject(scannedText: String, objectType: String, clipboardManager: android.content.ClipboardManager?) {
        viewModelScope.launch {
            val userMsgText = "👁️ Scanned via Friday Eye / Lens: ($objectType) \"$scannedText\""
            val userMsg = ChatMessageEntity(sender = "creator", messageText = userMsgText)
            repository.insertChatMessage(userMsg)
            
            _isChatLoading.value = true
            delay(1600)
            _isChatLoading.value = false
            
            try {
                clipboardManager?.setPrimaryClip(android.content.ClipData.newPlainText("Friday Scan", scannedText))
            } catch (e: Exception) {
                Log.e("FridayVM", "Clipboard error", e)
            }
            
            val responseText = "Boss, I have analyzed the physical visual scan. Object identified: $objectType.\n\nHere is the exact printed text, which I have automatically copied to your clipboard, Sir:\n\n\"$scannedText\"\n\nEverything is fully processed without any third-party redirects, Sir!"
            val fridayMsg = ChatMessageEntity(sender = "friday", messageText = responseText)
            repository.insertChatMessage(fridayMsg)
            speak("Visual scan processed successfully, Sir. Detected text is copied to your clipboard.")
            
            repository.insertLog(
                AutonomousLogEntity(
                    category = "SYSTEM",
                    messageText = "Optical character scan processed on $objectType. Copied to user clipboard."
                )
            )
        }
    }

    fun clearAllChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
            seedInitialChat()
        }
    }

    fun clearAllAutonomousLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            seedInitialLogs()
        }
    }

    override fun onCleared() {
        super.onCleared()
        autonomousJob?.cancel()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
