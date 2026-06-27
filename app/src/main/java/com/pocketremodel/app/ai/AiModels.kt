package com.pocketremodel.app.ai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- Request shape (what we send to OpenRouter) ----------

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.4,
    @SerialName("max_tokens") val maxTokens: Int = 400,
    // Ask OpenRouter to bias toward valid JSON where the model supports it.
    @SerialName("response_format") val responseFormat: ResponseFormat? = ResponseFormat()
)

@Serializable
data class ResponseFormat(val type: String = "json_object")

@Serializable
data class ChatMessage(val role: String, val content: String)

// ---------- Response shape (what OpenRouter sends back) ----------

@Serializable
data class ChatResponse(
    val choices: List<Choice> = emptyList(),
    val error: ApiError? = null
)

@Serializable
data class Choice(val message: ChatMessage? = null)

@Serializable
data class ApiError(val message: String = "", val code: String? = null)

// ---------- The AI's parsed design instruction ----------

@Serializable
data class AiPlan(
    val say: String = "",
    val actions: List<AiAction> = emptyList()
)

@Serializable
data class AiAction(
    val type: String,
    val catalogId: String? = null,
    val nodeId: String? = null,
    val description: String? = null,
    val placement: String? = null,
    val color: String? = null
)
