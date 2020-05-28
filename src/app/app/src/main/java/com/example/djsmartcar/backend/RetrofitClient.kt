package com.example.djsmartcar.backend

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "" // Add car IP address with http:// in front
    private const val SPOTIFY_URL = "https://api.spotify.com"
    private const val SPOTIFY_AUTH_URL = "https://accounts.spotify.com"

    // Making sure it's threadsafe for the singleton pattern
    // Used for sending requests to the SmartCar.
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

    // Used for sending requests to the Spotify Web API.
    val spotifyAPI: Endpoint by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(SPOTIFY_URL)
            .build()
        retrofit.create(Endpoint::class.java)
    }

    // Used for authorization to the Spotify Web API.
    val spotifyAuth: Endpoint by lazy {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(SPOTIFY_AUTH_URL)
            .build()
        retrofit.create(Endpoint::class.java)
    }
}