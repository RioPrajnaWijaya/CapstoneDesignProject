package com.example.capstonedesignproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.capstonedesignproject.chatbot.ChatScreen
import com.example.capstonedesignproject.chatbot.ChatViewModel

class ChatbotActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            // Obtains the ChatViewModel instance using viewModel() function from Compose
            val chatViewModel = viewModel<ChatViewModel>()

            // Displays the ChatScreen composable passing the ChatViewModel instance
            ChatScreen(chatViewModel)

        }
    }
}
