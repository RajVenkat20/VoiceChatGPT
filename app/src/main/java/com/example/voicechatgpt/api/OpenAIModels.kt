package com.example.voicechatgpt.api

data class OpenAIRequest(
    val prompt: String,
    val max_tokens: Int = 150
)

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val text: String
)
