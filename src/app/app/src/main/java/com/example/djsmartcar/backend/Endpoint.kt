package com.example.djsmartcar.backend

import retrofit2.Call
import retrofit2.http.*

interface Endpoint {

    @GET("/dance")
    fun getDance(@Query("id")id: String,
                 @Query("speed") speed: Int?,
                 @Query("delay") delay: Int?): Call<Void>
}