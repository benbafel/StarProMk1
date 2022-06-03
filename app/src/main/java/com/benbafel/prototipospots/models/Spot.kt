package com.benbafel.prototipospots.models
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
data class Spot (val id: String,
                 var name: String,
                 var description: String?,
                 var createdBy: String,
                 var lat: Double,
                 var lng: Double,
                 var bortleCenter: Int?,
                 var maxBortle: Int?,
                 var accesibility: Int?,
                 var spotQuality: Int?
                 //route:
                 ) :Serializable{}
/*if(placelatLng == markerlatLng){
                spotsRef.whereEqualTo("id",place.id)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result){
                            val dialog =
                                AlertDialog.Builder(this)
                                    .setMessage("${document.data}")
                                    .setPositiveButton("ok",null)
                                    .show()
                        }

                    }

            }

            private fun getPlaceFromPlaces(markerInfo: Marker) {
        val db = Firebase.firestore
        val spotsRef = db.collection("spots")
        for (place in places){
            val placeLat = place.latitude
            val placeLng = place.longitude
            val placelatLng = LatLng(placeLat,placeLng)
            val markerLat = markerInfo.position.latitude
            val markerLng = markerInfo.position.longitude
            val markerlatLng = LatLng(markerLat,markerLng)

            if(placelatLng == markerlatLng){
                spotsRef.whereEqualTo("id",place.id)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result){
                            val dialog =
                                AlertDialog.Builder(this)
                                    .setMessage("${document.data}")
                                    .setPositiveButton("ok",null)
                                    .show()
                        }

                    }

            }

        }
    }*/






