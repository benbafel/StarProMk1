package com.benbafel.prototipospots.models
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
data class Spot(
    val id: String,
    var name: String?,
    var description: String?,
    var createdBy: String?,
    var lat: Double,
    var lng: Double,
    var bortleCenter: Int?,
    var maxBortle: Int?,
    var accesibility: Int?,
    var spotQuality: Float?
                 //route:
                 ) :Serializable






