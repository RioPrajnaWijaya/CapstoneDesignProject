package com.example.capstonedesignproject.chatbot

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Interface defining the endpoints for communicating with the OpenAI API
interface OpenAIApi {
    @Headers("Content-Type: application/json", "Authorization: Bearer YOUR_API_KEY")
    @POST("v1/chat/completions")
    suspend fun generateResponse(@Body requestBody: OpenAIRequestBody): OpenAIResponse
}

// Data class representing the request body for the OpenAI API
data class OpenAIRequestBody(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val max_tokens: Int = 2048,
    val n: Int = 1,
    val temperature: Double = 1.0
)

data class OpenAIResponse(
    val choices: List<MessageResponse>
)

data class MessageResponse(
    val message: Message
)