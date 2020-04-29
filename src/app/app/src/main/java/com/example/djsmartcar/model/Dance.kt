package com.example.djsmartcar.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Dance(
    @SerializedName("id")
    val danceId: String? = ""
) : Parcelable