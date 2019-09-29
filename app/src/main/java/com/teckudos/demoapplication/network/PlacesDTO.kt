package com.teckudos.demoapplication.network

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PlacesDTO(
    val results: List<Result>,
    val status: String
) : Parcelable {}

@Parcelize
data class Result(
    val geometry: Geometry,
    val icon: String,
    val id: String,
    val name: String,
    @Json(name = "place_id") val placeId: String,
    val rating: Double,
    val reference: String,
    val scope: String,
    val types: List<String>,
    val vicinity: String
) : Parcelable {}

@Parcelize
data class Geometry(
    val location: Location
) : Parcelable {}

@Parcelize
data class Location(
    val lat: Double,
    val lng: Double
) : Parcelable {}