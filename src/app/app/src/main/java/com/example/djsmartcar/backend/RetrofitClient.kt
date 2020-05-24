package com.example.djsmartcar.backend

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

// Use object for singleton
object RetrofitClient {

    private const val BASE_URL = "" // Add car URL

    // Making sure it's threadsafe for the singleton pattern
    // Lazy is a kotlin feature that ensures the instance is only executed once
    val instance: Endpoint by lazy {
        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .build()

        retrofit.create(Endpoint::class.java)
    }

    val spotifyAPI: Endpoint by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.spotify.com")
            .build()
        retrofit.create(Endpoint::class.java)
    }

    val spotifyAuth: Endpoint by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://accounts.spotify.com")
            .build()
        retrofit.create(Endpoint::class.java)
    }
}