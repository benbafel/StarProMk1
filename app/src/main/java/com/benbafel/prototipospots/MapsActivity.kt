@file:Suppress("DEPRECATION")

package com.benbafel.prototipospots

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.benbafel.prototipospots.databinding.ActivityMapsBinding
import com.benbafel.prototipospots.models.Spot
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

private const val TAG = "MapsActivity"
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var infoShown: Boolean = true
    private var lastMarkerPosition = LatLng(-0.00, -0.00)
    val db = Firebase.firestore
    private lateinit var mMap: GoogleMap
    private var markers: MutableList<Marker> = mutableListOf()
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.view?.let {
            Snackbar.make(it,"Long press to add a marker!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok",{})
                .setActionTextColor(ContextCompat.getColor(this,android.R.color.white))
                .show()
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val boundsBuilder = LatLngBounds.builder()
        //Display Markers on the map, based on input list of places
        var  spots : MutableList<Spot> = mutableListOf()
        loadSpots(mMap,db,spots)


      /*  for(spot){
            val latLng = LatLng(spot.latitude,spot.longitude)
            boundsBuilder.include(latLng)
            mMap.addMarker(MarkerOptions().position(latLng).title(spot.title).snippet(spot.description))

        }  */

        mMap.setOnMarkerClickListener{marker ->

            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            when {
                (lastMarkerPosition == marker.position && infoShown) -> {//V : V = F
                    Log.i(TAG,"hide same point")
                    marker.hideInfoWindow()
                    infoShown = !infoShown
                }
                lastMarkerPosition == marker.position && !infoShown ->{
                    Log.i(TAG,"show same point")
                    marker.showInfoWindow()
                    infoShown = !infoShown
                }
                lastMarkerPosition != marker.position && infoShown -> {//F : V = V
                    Log.i(TAG,"show different point, infoShown")
                    marker.showInfoWindow()
                    lastMarkerPosition = marker.position
                }
                else -> {//F : F = V
                    Log.i(TAG,"show different point, Not InfoShown")
                    marker.showInfoWindow()
                    lastMarkerPosition =marker.position
                    infoShown = !infoShown
                }
            }

            true
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(),1000,1000,0))


        mMap.setOnInfoWindowClickListener { markerToDelete ->
            Log.i(TAG,"OnInfoWindowClickListener- Delete")
            markers.remove(markerToDelete)
            markerToDelete.remove()
        }


        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun loadSpots(mMap: GoogleMap, db: FirebaseFirestore, spots: MutableList<Spot>)  {
        db.collection("spots")
            .get()
            .addOnSuccessListener { documents ->
                val boundsBuilder = LatLngBounds.builder()
                for (document in documents){
                    Log.i(TAG,"${document.id} => ${document.data}")
                    val spot = document.toObject<Spot>()
                    spots.add(spot)
                    val latLng = LatLng(spot.latitude,spot.longitude)
                    boundsBuilder.include(latLng)
                    mMap.addMarker(MarkerOptions().position(latLng).title(spot.name).snippet("Clic aquí para ver detalles del punto"))
                }

            }

            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)

            }
    }


}