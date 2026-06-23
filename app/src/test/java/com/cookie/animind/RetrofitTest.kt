package com.cookie.animind

import com.cookie.animind.data.remote.ApiClient
import com.cookie.animind.data.remote.Content
import com.cookie.animind.data.remote.GenerateContentRequest
import com.cookie.animind.data.remote.Part
import kotlinx.coroutines.runBlocking
import org.junit.Test
import retrofit2.HttpException

class RetrofitTest {
    @Test
    fun testGemini404() = runBlocking {
        try {
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = "Hello"))))
            )
            val response = ApiClient.geminiService.generateContent("invalid_key", request)
            println("Success: $response")
        } catch (e: HttpException) {
            println("HTTP Error: ${e.code()} - ${e.message()}")
            val errorBody = e.response()?.errorBody()?.string()
            println("Body: $errorBody")
            assert(e.code() != 404) { "URL is 404 Not Found! The URL or model name is wrong in Retrofit." }
        } catch (e: Exception) {
            println("Other Error: ${e.message}")
        }
    }
}
