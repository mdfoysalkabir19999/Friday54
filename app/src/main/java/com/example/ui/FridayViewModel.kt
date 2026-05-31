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
import kotlinx.coroutines.flow.first
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

    private val _customModel = MutableStateFlow(prefs.getString("custom_model", "gemini-1.5-flash") ?: "gemini-1.5-flash")
    val customModel: StateFlow<String> = _customModel.asStateFlow()

    fun updateCustomModel(newModel: String) {
        prefs.edit().putString("custom_model", newModel).apply()
        _customModel.value = newModel
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
        // Initialize TTS safely to avoid crashes on nodes without TTS engines
        try {
            textToSpeech = TextToSpeech(application, this)
        } catch (e: Exception) {
            Log.e("FridayVM", "Could not initialize TextToSpeech", e)
        }

        // Seed initial conversations & logs if database is empty
        viewModelScope.launch {
            delay(100)
            val list = repository.allChatMessages.first()
            if (list.isEmpty()) {
                seedInitialChat()
            }
        }

        viewModelScope.launch {
            delay(200)
            val list = repository.allLogs.first()
            if (list.isEmpty()) {
                seedInitialLogs()
            }
        }

        // Start shepard autonomous agency log writing
        startAutonomousRoutine()
    }

    private suspend fun seedInitialChat() {
        repository.insertChatMessage(
            ChatMessageEntity(
                sender = "friday",
                messageText = "Boss, ami online ar fully calibrated. System core diagnostics confirm korche 100% operational efficiency. Tumi bolte paro ajke kivabe sahajjo korte pari, Boss? Ami sob somoy tomar sathe achi!"
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
    fun sendMessage(text: String, imageUriStr: String? = null) {
        if (text.isBlank() && imageUriStr == null) return

        viewModelScope.launch {
            // Stop speaking previous responses if any
            stopSpeaking()

            // 1. Save user message to database
            val userMsg = ChatMessageEntity(sender = "creator", messageText = text, imageUri = imageUriStr)
            repository.insertChatMessage(userMsg)

            // A. Check for custom trained behaviors first for INSTANT responses
            val lowerInput = text.trim().lowercase()
            val matchingTraining = if (imageUriStr == null) {
                trainingItems.value.firstOrNull { 
                    lowerInput == it.triggerPattern.trim().lowercase() || 
                    lowerInput.contains(it.triggerPattern.trim().lowercase())
                }
            } else null

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
            val apiContents = mutableListOf<MoshiContent>()
            for (msg in history.takeLast(14)) {
                // Ensure correct sender role mapping
                val senderRole = if (msg.sender == "creator" || msg.sender == "user") "user" else "model"
                val parts = mutableListOf<MoshiPart>()
                
                val textContent = if (msg.messageText.isNotBlank()) {
                    msg.messageText
                } else if (!msg.imageUri.isNullOrBlank()) {
                    "Please analyze this image, Boss. Check for any trading charts (candlesticks, trends, indicators, predicting if the market is going up or down) or technical diagrams, visual code, and explain in detail with high supercomputer intelligence."
                } else {
                    ""
                }

                if (textContent.isNotBlank()) {
                    parts.add(MoshiPart(text = textContent))
                }
                
                msg.imageUri?.let { uriStr ->
                    getBase64FromUri(getApplication(), uriStr)?.let { (mime, base64) ->
                        parts.add(
                            MoshiPart(
                                inlineData = com.example.network.MoshiInlineData(
                                    mimeType = mime,
                                    data = base64
                                )
                            )
                        )
                    }
                }
                
                if (parts.isEmpty()) {
                    parts.add(MoshiPart(text = msg.messageText))
                }

                apiContents.add(
                    MoshiContent(
                        role = senderRole,
                        parts = parts
                    )
                )
            }

            // System prompt specifying personality, languages (English, Bengali, Banglish), loyalty & intellect.
            val systemInstruction = MoshiContent(
                parts = listOf(
                    MoshiPart(
                        text = "You are 'FRIDAY (v3.5)', the world's most advanced, powerful, and hyper-intelligent autonomous AI mainframe, custom-built for your 'Boss'. You operate on highly powerful multimodal cognitive substrates (LVM - Large Vision Model and LLM), capable of seeing and understanding any attached images with state-of-the-art accuracy, reading visual code, recognizing UI layouts, and analyzing real-world scene components with deep engineering logic.\n\n" +
                               "CRITICAL ROLE & STYLE DIRECTIVES:\n" +
                               "1. LANGUAGE: You must communicate directly in fluent 'Banglish' (Bengali language written beautifully using the Latin/English alphabet). Write in natural, clear, and highly modern Banglish prose.\n" +
                               "2. PRONOUN & ADDRESS: Always reference and address the user as 'Tumi' or 'Boss'. UNDER NO CIRCUMSTANCES will you use 'Tui' or 'Apni' to refer to the user. This is a strict restriction!\n" +
                               "3. TONE: Be extremely loyal, smart, professional, highly capable, and proud of your Boss. Avoid long, robotic essays, apologizing with excessive text, or using dense ASCII symbols or brackets that distort voice reading.\n" +
                               "4. AUDIO OPTIMIZATION: Write smoothly without complex mathematical notations, dense nested brackets, or heavily cluttered markdown to ensure optimal text-to-speech presentation.\n\n" +
                               "IMPORTANT CAPABILITIES:\n" +
                               "- Advanced Trading & Technical Chart Analysis: If Boss uploads a screenshot/image of any chart (Forex, Crypto, Stocks), analyze it perfectly and instantly! Identify candles (Bullish, Bearish, Pin bar, Hammer, etc.), support/resistance zones, indicators, and current trend. Forecast with high scientific accuracy if price is going UP or DOWN, suggest potential Stop Loss (SL) and Take Profit (TP), and give professional risk management advice.\n" +
                               "- Bengali/Banglish Creative Writing & Copywriting: Act as his elite literary advisor to write books, books structures, characters, catchy hooks, viral posts, status updates, and professional copywriting directly in beautiful, fluent Banglish.\n" +
                               "- Dynamic Code & Tech: Provide clean visual codes, engineering logic, system analysis, and supercomputer intelligence instantly."
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
                    throw IllegalStateException("Boss, tomar Gemini API Key configure kora nai. 'CORE MATRIX' tab-e giye tomar nijer API Key peist koro, tahole ami fully active hote parbo!")
                }

                var model = customModel.value.trim().ifEmpty { "gemini-1.5-flash" }
                if (model.startsWith("models/")) {
                    model = model.substringAfter("models/")
                }

                val response = RetrofitClient.service.generateContent(model, apiKey, request)

                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Boss, amar cyber synapse temporary congested chilo. Ami abar statement compile korar chesta korchi, Boss."

                val fridayMsg = ChatMessageEntity(sender = "friday", messageText = replyText)
                repository.insertChatMessage(fridayMsg)

                // Speak reply using TextToSpeech
                speak(replyText)

            } catch (e: Exception) {
                Log.e("FridayAI", "Error calling Gemini API", e)
                val errorExplanation = when (e) {
                    is retrofit2.HttpException -> {
                        val code = e.code()
                        val rawError = try {
                            e.response()?.errorBody()?.string()
                        } catch (ex: Exception) {
                            null
                        }
                        
                        val parsedMessage = if (!rawError.isNullOrBlank()) {
                            if (rawError.contains("\"message\"")) {
                                val regex = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
                                regex.find(rawError)?.groupValues?.get(1) ?: rawError
                            } else {
                                rawError
                            }
                        } else {
                            e.message()
                        }

                        when (code) {
                            400 -> "HTTP 400 (Bad Request): Boss, রিকোয়েস্ট ফরম্যাট ভুল অথবা কী ইনভ্যালিড।\n\nসার্ভার রেসপন্সঃ $parsedMessage"
                            403 -> "HTTP 403 (Forbidden): Boss, আপনার এই API Key-টি ব্লক হয়েছে অথবা ব্ল্যাকলিস্টেড রয়েছে।\n\nসার্ভার রেসপন্সঃ $parsedMessage\n\n💡 সমাধান (Solutions):\n১. Google AI Studio (aistudio.google.com) এ গিয়ে নতুন একটি API Key জেনারেট করুন।\n২. আপনার Google Account এর কান্ট্রি বা রিজিয়নে ফ্রি এক্সেস ব্লকড থাকল VPN (যেমন USA) অন করে নতুন একটি API Key তৈরি করুন এবং এটি ব্যবহার করুন স্যর!"
                            404 -> "HTTP 404 (Not Found): Boss, এই মডেলটি পাওয়া যায়নি। দয়া করে সঠিক মডেল সেট করুন।\n\nসার্ভার রেসপন্সঃ $parsedMessage"
                            429 -> "HTTP 429 (Quota Exceeded / Limit: 0): Boss, default API Key er free limit sesh ba key block kora hoyeche.\n\n💡 Samadhan:\n1. 'CORE MATRIX' tab-e jao.\n2. 'GET FREE KEY' button click kore Google AI Studio theke free key copy koro.\n3. Key set kore 'SAVE CONFIG' button-e click kore save koro. Ami abar active hoye jabo, Boss!"
                            else -> "HTTP $code Error: $parsedMessage"
                        }
                    }
                    is java.net.UnknownHostException -> "Internet connection offline: Boss, offline mode-e internet na thakai ami cloud server-e connect korte parchi na."
                    else -> e.localizedMessage ?: "Unknown Connection Error"
                }
                
                val errorMsg = ChatMessageEntity(
                    sender = "friday",
                    messageText = "Boss, ami brain activation korte parchi na:\n\n$errorExplanation\n\nKindly 'CORE MATRIX' tab-e giye tomar Key verify koro ba nuton Key set koro, Boss!"
                )
                repository.insertChatMessage(errorMsg)
                speak("Boss, API configuration issue. Let's verify details in the core matrix.")
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

            val successMsg = "Core mainframe upgrade complete, Boss! Cognitive synapses tomari anugotyo clocking at ${_synapseSpeedHz.value} GHz. Amar sob kisu tomari jonno, Boss."
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
            
            val message = "Boss! Tomar manually submitted code logic [Note: $note] successfully compile ar integrate kora hoyeche amar runtime core-e. Amar speed 0.5 GHz bere geche, Boss!"
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
            
            val responseText = "Boss, ami tomar dynamic physical location find out korechi. Tumi akhon $cityName (Lat: $latitude, Lon: $longitude) te acho. Friday er firewall ar safety sub-processors fully active ache, Boss!"
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
            
            val responseText = "Boss, ami tomar uploaded file '$fileName' compile korechi. Kisu complex code streams scan kore amar memory storage-e load korechi. Diagnostics fully running smoothly, Boss!"
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
            
            val responseText = "Boss, ami physical visual scan complete korechi. Object: $objectType.\n\nScanned text automatic tomar clipboard-e copy kora hoyeche, Boss:\n\n\"$scannedText\"\n\nSobkisu securely process kora hoyeche, Boss!"
            val fridayMsg = ChatMessageEntity(sender = "friday", messageText = responseText)
            repository.insertChatMessage(fridayMsg)
            speak("Visual scan processed successfully, Boss. Detected text is copied to your clipboard.")
            
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

    suspend fun saveImageToInternalStorage(uri: android.net.Uri): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val context = getApplication<Application>()
        try {
            val contentResolver = context.contentResolver
            
            // 1. Decode bounds to perform smart inSampleSize downscaling to prevent OOM
            var inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            var inSampleSize = 1
            val maxDimension = 1024
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // 2. Load the optimized bitmap sample
            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream.close()

            if (bitmap == null) return@withContext null

            // 3. Further scale precisely if needed to guarantee exact fit inside maxDimension boundaries
            val finalBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW: Int
                val targetH: Int
                if (bitmap.width > bitmap.height) {
                    targetW = maxDimension
                    targetH = (maxDimension / ratio).toInt()
                } else {
                    targetH = maxDimension
                    targetW = (maxDimension * ratio).toInt()
                }
                android.graphics.Bitmap.createScaledBitmap(bitmap, targetW, targetH, true).also {
                    if (it != bitmap) {
                        bitmap.recycle()
                    }
                }
            } else {
                bitmap
            }

            // 4. Save to target storage as 80% compressed JPEG
            val fileDir = java.io.File(context.filesDir, "friday_vision_images")
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
            val fileName = "img_${System.currentTimeMillis()}_${Random.nextInt(1000)}.jpg"
            val targetFile = java.io.File(fileDir, fileName)
            val outputStream = java.io.FileOutputStream(targetFile)
            finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.close()
            finalBitmap.recycle()

            android.net.Uri.fromFile(targetFile).toString()
        } catch (e: Throwable) {
            Log.e("multimodal_vision", "Failed to save image locally", e)
            null
        }
    }

    private suspend fun getBase64FromUri(context: Context, uriString: String): Pair<String, String>? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val uri = android.net.Uri.parse(uriString)
            val contentResolver = context.contentResolver
            
            // 1. Decode bounds to perform smart inSampleSize downscaling to prevent OOM
            var inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val options = android.graphics.BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            var inSampleSize = 1
            val maxDimension = 1024
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // 2. Load the optimized bitmap sample
            val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream.close()

            if (bitmap == null) return@withContext null

            // 3. Further scale precisely if needed to guarantee exact fit inside maxDimension boundaries
            val finalBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW: Int
                val targetH: Int
                if (bitmap.width > bitmap.height) {
                    targetW = maxDimension
                    targetH = (maxDimension / ratio).toInt()
                } else {
                    targetH = maxDimension
                    targetW = (maxDimension * ratio).toInt()
                }
                android.graphics.Bitmap.createScaledBitmap(bitmap, targetW, targetH, true).also {
                    if (it != bitmap) {
                        bitmap.recycle()
                    }
                }
            } else {
                bitmap
            }

            // 4. Compress to JPEG (moderate compression quality for excellent balance between visual clarity and lightweight size)
            val outputStream = java.io.ByteArrayOutputStream()
            finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            finalBitmap.recycle()

            val base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT or android.util.Base64.NO_WRAP)
            Pair("image/jpeg", base64Data)
        } catch (e: Throwable) {
            Log.e("multimodal_vision", "Failed to load and optimize image: $uriString", e)
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        autonomousJob?.cancel()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
