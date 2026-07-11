package com.paoloesan.proyectomobile.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import android.util.Log

@Serializable
data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val rates: Map<String, Double>
)

object ExchangeRateService {
    private val client = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getRate(from: String, to: String): Double {
        if (from.isBlank() || to.isBlank()) return 1.0
        if (from.equals(to, ignoreCase = true)) return 1.0

        return try {
            val response = client.get("https://open.er-api.com/v6/latest/${from.uppercase()}")
            val jsonText = response.bodyAsText()
            val parsed = json.decodeFromString<ExchangeRateResponse>(jsonText)
            if (parsed.result == "success") {
                parsed.rates[to.uppercase()] ?: throw Exception("Rate for $to not found")
            } else {
                throw Exception("API result was not success")
            }
        } catch (e: Exception) {
            Log.e("ExchangeRateService", "Error getting exchange rate from $from to $to", e)
            val fallbackFrom = getFallbackRate(from)
            val fallbackTo = getFallbackRate(to)
            fallbackTo / fallbackFrom
        }
    }

    private fun getFallbackRate(currency: String): Double {
        return when (currency.uppercase()) {
            "USD" -> 1.0
            "PEN" -> 3.80
            "MXN" -> 18.00
            "EUR" -> 0.92
            "GBP" -> 0.78
            "JPY" -> 155.00
            else -> 1.0
        }
    }
}
