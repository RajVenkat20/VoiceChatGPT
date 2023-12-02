package com.example.voicechatgpt.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //private const val BASE_URL = "https://api.openai.com/"
    private const val BASE_URL = "https://api.openai.com/v1/engines/davinci/completions/"

    val instance: OpenAIApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(OpenAIApiService::class.java)
    }
}
