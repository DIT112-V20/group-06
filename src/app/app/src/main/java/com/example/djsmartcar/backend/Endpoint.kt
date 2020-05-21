package com.example.djsmartcar.backend

import com.example.djsmartcar.model.Dance
import retrofit2.Call
import retrofit2.http.*

interface Endpoint {

    // @Headers

    @GET("/dance")
    fun getDance(@Query("id")id: String,
                 @Query("speed") speed: Int?,
                 @Query("delay") delay: Int?): Call<Void>
}