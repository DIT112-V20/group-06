package com.example.djsmartcar.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Dance(
    @SerializedName("id")
    val id: String? = "",
    @SerializedName("speed")
    val speed: Int? = 0,
    @SerializedName("delay")
    val delay: Int? = 0
) : Parcelable