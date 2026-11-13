package com.jarvis.assistant
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        message.contains("hello", ignoreCase = true) ->
            "Hello! I am JARVIS. How can I help you?"

        message.contains("your name", ignoreCase = true) ->
            "I am JARVIS, your personal assistant."

        message.contains("who are you", ignoreCase = true) ->
            "I am JARVIS, your personal assistant."

        message.contains("how are you", ignoreCase = true) ->
            "All systems are running normally."

        message.contains("help", ignoreCase = true) ->
            "Certainly! Tell me what you need help with."

        else ->
            "I received your message: \"$message\". My AI connection is not enabled yet."
    }
}

@Composable
fun JarvisScreen() {
    var message by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf<ChatMessage>()
    }

    val background = Color(0xFF050B16)
    val cyan = Color(0xFF00D9FF)

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = cyan,
            background = background,
            surface = Color(0xFF101C2C)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = "J.A.R.V.I.S",
                    color = cyan,
                    fontSize = 32.sp
                )

                Text(
                    text = "PERSONAL AI ASSISTANT",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(25.dp))

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            Color(0xFF10283B),
                            RoundedCornerShape(100.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "J",
                        color = cyan,
                        fontSize = 65.sp
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Text(
                    text = "SYSTEM ONLINE",
                    color = cyan,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                                    text = if (chat.isUser)
                                        "YOU"
                                    else
                                        "JARVIS",
                                    color = cyan,
                                    fontSize = 12.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(5.dp)
                                )

                                Text(
                                    text = chat.text,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ask JARVIS") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val question = message.trim()

                        if (question.isNotEmpty()) {
                            messages.add(
                                ChatMessage(question, true)
                            )

                            messages.add(
                                ChatMessage(
                                    getDemoReply(question),
                                    false
                                )
                            )

                            message = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SEND")
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
