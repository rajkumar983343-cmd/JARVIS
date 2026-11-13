package com.jarvis.assistant
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.util.Locale

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JarvisScreen()
        }
    }
}

fun getDemoReply(message: String): String {
    return when {
        message.contains("hello", true) ||
        message.contains("hi", true) ||
        message.contains("hyy", true) ->
            "Hello! I am JARVIS. How can I help you?"

        message.contains("your name", true) ||
        message.contains("who are you", true) ->
            "I am JARVIS, your personal assistant."

        message.contains("how are you", true) ->
            "All systems are running normally."

        message.contains("time", true) ->
            "I am ready to assist you."

        message.contains("help", true) ->
            "Certainly! Tell me what you need help with."

        else ->
            "I received your message: $message. My AI connection is not enabled yet."
    }
}

@Composable
fun JarvisScreen() {
    var message by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("SYSTEM ONLINE") }
    var isMuted by remember { mutableStateOf(false) }
    var speechRecognizer by remember {
        mutableStateOf<SpeechRecognizer?>(null)
    }
    var tts by remember {
        mutableStateOf<TextToSpeech?>(null)
    }

    val messages = remember {
        mutableStateListOf<ChatMessage>()
    }

    val context = LocalContext.current
    val cyan = Color(0xFF00D9FF)
    val background = Color(0xFF050B16)
    val handler = remember { Handler(Looper.getMainLooper()) }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )
            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )
        }
    }

    fun startListening() {
        if (isMuted) return

        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            status = "MICROPHONE PERMISSION REQUIRED"
            return
        }

        try {
            status = "LISTENING..."
            speechRecognizer?.startListening(speechIntent)
        } catch (e: Exception) {
            status = "VOICE START ERROR"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                startListening()
            } else {
                status = "MICROPHONE PERMISSION DENIED"
            }
        }

    DisposableEffect(context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            status = "VOICE RECOGNITION UNAVAILABLE"
            onDispose { }
        } else {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        if (!isMuted) status = "LISTENING..."
                    }

                    override fun onBeginningOfSpeech() {
                        status = "HEARING YOU..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        status = "PROCESSING..."
                    }

                    override fun onError(error: Int) {
                        if (!isMuted) {
                            status = "RESTARTING LISTENING..."
                            handler.postDelayed({
                                startListening()
                            }, 800)
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        val spokenText = results
                            ?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )
                            ?.firstOrNull()

                        if (!spokenText.isNullOrBlank()) {
                            messages.add(
                                ChatMessage(spokenText, true)
                            )

                            val reply = getDemoReply(spokenText)

                            messages.add(
                                ChatMessage(reply, false)
                            )

                            status = "JARVIS RESPONDING..."

                            tts?.speak(
                                reply,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "jarvis_reply"
                            )
                        } else if (!isMuted) {
                            handler.postDelayed({
                                startListening()
                            }, 500)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                }
            )

            onDispose {
                handler.removeCallbacksAndMessages(null)
                recognizer.destroy()
                speechRecognizer = null
            }
        }
    }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { result ->
            if (result == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }

        tts = engine

        engine.setOnUtteranceProgressListener(
            object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    handler.post {
                        if (!isMuted) {
                            status = "LISTENING..."
                            startListening()
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    handler.post {
                        if (!isMuted) startListening()
                    }
                }
            }
        )

        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
        }
    }

    LaunchedEffect(Unit) {
        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            startListening()
        } else {
            permissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    fun sendMessage() {
        val question = message.trim()

        if (question.isNotEmpty()) {
            messages.add(ChatMessage(question, true))

            val reply = getDemoReply(question)
            messages.add(ChatMessage(reply, false))

            tts?.speak(
                reply,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "jarvis_reply"
            )

            message = ""
            status = "JARVIS RESPONDING..."
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(primary = cyan)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "J.A.R.V.I.S",
                    color = cyan,
                    fontSize = 32.sp
                )

                Text(
                    "PERSONAL AI ASSISTANT",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(105.dp)
                        .background(
                            Color(0xFF10283B),
                            RoundedCornerShape(100.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "J",
                        color = cyan,
                        fontSize = 65.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    status,
                    color = cyan,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        isMuted = !isMuted

                        if (isMuted) {
                            stopListening()
                            tts?.stop()
                            status = "MICROPHONE MUTED"
                        } else {
                            startListening()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isMuted) "UNMUTE JARVIS" else "MUTE JARVIS"
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { chat ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (chat.isUser)
                                Color(0xFF10334A)
                            else
                                Color(0xFF101C2C),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    if (chat.isUser) "YOU" else "JARVIS",
                                    color = cyan,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(5.dp))

                                Text(
                                    chat.text,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Ask JARVIS") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { sendMessage() }
                    ) {
                        Text("SEND")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
