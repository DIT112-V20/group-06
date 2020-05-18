package com.example.djsmartcar.backend

import com.example.djsmartcar.model.AuthToken
import com.example.djsmartcar.model.Dance
import retrofit2.Call
import retrofit2.http.*

interface Endpoint {

    // @Headers

    @GET("/dance")
    fun getDance(@Query("id")id: String): Call<List<Dance>>

    @GET("/random")
    fun getRandom(): Call<List<Dance>>

    @GET("/stop")
    fun getStop(): Call<List<Dance>>

    // The stuff in basic is BASE64(client_id:client_secret)
    // Can be hardcoded, use https://www.base64encode.org/ or just generated on the fly by java :)
    @FormUrlEncoded
    @Headers("Authorization: Basic ZmYwZmY1M2I1MWM5NGM2YWFmNWNjOTE2MGM1YjViZmI6ZWRjY2JhOTJjMjBkNGUwNTg3MjdjYTFjYTBmMDk2NDY=")
    @POST("/api/token")
    fun getSpotifyAPIToken(@Field("grant_type") grant_type: String): Call<AuthToken>

//    @GET("/v1/audio-analysis/{id} ")
//    fun getTrackInfo(@Path("id")id: String): Call<List<Track>>
}