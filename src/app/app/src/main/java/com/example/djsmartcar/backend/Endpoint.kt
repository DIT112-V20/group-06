package com.example.djsmartcar.backend

import com.example.djsmartcar.model.Dance
import retrofit2.Call
import retrofit2.http.*

interface Endpoint {

    // @Headers

    @GET("dance?id={id}")
    fun getDance(@Path("id") danceId: String): Call<List<Dance>>

}