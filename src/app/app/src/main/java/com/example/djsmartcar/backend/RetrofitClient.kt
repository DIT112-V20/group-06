package com.example.djsmartcar.backend

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// Use object for singleton
object RetrofitClient {

    private const val BASE_URL = "" // Add car URL

    // Making sure it's threadsafe for the singleton pattern
    // Configuration stuff from retrofit
    // Lazy is a kotlin feature that ensures the instance is only executed once
    val instance: Endpoint by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create()) // Parses the JSON
            .baseUrl(BASE_URL)
            .build()

        retrofit.create(Endpoint::class.java)
    }

    private const val SPOTIFY_URL = "https://api.spotify.com/v1"

    val instance2: Endpoint by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create()) // Parses the JSON
            .baseUrl(SPOTIFY_URL)
            .build()

        retrofit.create(Endpoint::class.java)
    }


}