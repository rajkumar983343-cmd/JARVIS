package com.jarvis.assistant
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
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

    val messages = remember {
        mutableStateListOf<ChatMessage>()
    }

    val context = LocalContext.current
    val cyan = Color(0xFF00D9FF)
    val background = Color(0xFF050B16)

    var tts by remember {
        mutableStateOf<TextToSpeech?>(null)
    }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { result ->
            if (result == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
            }
        }

        tts = engine

        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
        }
    }

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
                RecognizerIntent.EXTRA_PROMPT,
                "Speak to JARVIS"
            )
        }
    }

    val speechLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val spokenText = result.data
                ?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                ?.firstOrNull()

            if (!spokenText.isNullOrBlank()) {
                message = spokenText
                status = "VOICE RECEIVED"
            } else {
                status = "NO SPEECH DETECTED"
            }
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                status = "LISTENING..."
                speechLauncher.launch(speechIntent)
            } else {
                status = "MICROPHONE PERMISSION DENIED"
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
            status = "SYSTEM ONLINE"
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

                Spacer(modifier = Modifier.height(15.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
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

                                Spacer(
                                    modifier = Modifier.height(5.dp)
                                )

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
                        onClick = {
                            permissionLauncher.launch(
                                Manifest.permission.RECORD_AUDIO
                            )
                        },
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text("🎙")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { sendMessage() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SEND")
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
