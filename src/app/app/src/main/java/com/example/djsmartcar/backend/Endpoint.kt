package com.example.djsmartcar.backend

import com.example.djsmartcar.model.Dance
import retrofit2.Call
import retrofit2.http.*
import com.spotify.protocol.types.Track

interface Endpoint {

    // @Headers

    @GET("/dance")
    fun getDance(@Query("id")id: String): Call<List<Dance>>

    @GET("/random")
    fun getRandom(): Call<List<Dance>>

    @GET("/stop")
    fun getStop(): Call<List<Dance>>

    @GET("/track/{id}")
    fun getTrackInfo(@Path("id")id: String): Call<List<Track>>

}