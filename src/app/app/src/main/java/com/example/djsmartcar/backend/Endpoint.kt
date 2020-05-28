package com.example.djsmartcar.backend

import com.example.djsmartcar.model.*
import retrofit2.Call
import retrofit2.http.*

interface Endpoint {

    /**
     * Endpoint for the SmartCar
     * Example endpoint: /dance?id=1&speed=50&delay=500
     */
    @GET("/dance")
    fun getDance(@Query("id") id: String,
                 @Query("speed") speed: Int?,
                 @Query("delay") delay: Int?): Call<Void>

    /**
     * Endpoints for the Spotify Web API
     * Authorization: Basic *<base64 encoded client_id:client_secret>*
     * Base64(client_id:client_secret)
     * Can be hardcoded, use https://www.base64encode.org/ or just generated on the fly by java.
     */
    @FormUrlEncoded
    @Headers("Authorization: Basic ")
    @POST("/api/token")
    fun getSpotifyAPIToken(@Field("grant_type") grant_type: String): Call<AuthToken>

    @GET("/v1/audio-analysis/{id}")
    fun getTrackAnalysis(@Header("Authorization") bearerToken: String,
                         @Path("id") id: String): Call<AudioAnalysis>
}