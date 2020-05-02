package com.example.djsmartcar.backend

import com.example.djsmartcar.model.Dance
import retrofit2.Call
import retrofit2.http.*

interface Endpoint {

    // @Headers

    @GET("dance")
    fun getDance(@Query("id")danceId: String): Call<List<Dance>>

}