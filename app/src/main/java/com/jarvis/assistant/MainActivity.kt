
package com.jarvis.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            JarvisScreen()
        }
    }
}

@Composable
fun JarvisScreen() {
    var message by remember { mutableStateOf("") }

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
                Spacer(modifier = Modifier.height(30.dp))

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

                Spacer(modifier = Modifier.height(55.dp))

                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(
                            Color(0xFF10283B),
                            RoundedCornerShape(100.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "J",
                        color = cyan,
                        fontSize = 90.sp
                    )
                }

                Spacer(modifier = Modifier.height(25.dp))

                Text(
                    text = "SYSTEM ONLINE",
                    color = cyan,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Ask JARVIS") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { message = "" },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SEND")
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
