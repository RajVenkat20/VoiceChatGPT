package com.example.voicechatgpt.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/engines/davinci-codex/completions")
    fun getChatGPTResponse(
        @Header("Authorization") apiKey: String,
        @Body request: OpenAIRequest
    ): Call<OpenAIResponse>
}
