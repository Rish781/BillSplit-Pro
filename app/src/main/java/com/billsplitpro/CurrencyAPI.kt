package com.billsplitpro

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. The Data Model (What the internet sends us back)
data class ExchangeRates(
    val rates: Map<String, Double>
)

// 2. The Interface (The specific request we make)
interface CurrencyService {
    // We ask for rates based on Indian Rupee (INR)
    @GET("latest?from=INR")
    suspend fun getRates(): ExchangeRates
}

// 3. The Object (The actual connection builder)
object CurrencyAPI {
    private const val BASE_URL = "https://api.frankfurter.app/"

    val service: CurrencyService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyService::class.java)
    }
}