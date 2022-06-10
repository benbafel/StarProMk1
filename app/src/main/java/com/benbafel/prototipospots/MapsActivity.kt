@file:Suppress("DEPRECATION")

package com.benbafel.prototipospots

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.benbafel.prototipospots.R.id.map
import com.benbafel.prototipospots.databinding.ActivityMapsBinding
import com.benbafel.prototipospots.models.Comment
import com.benbafel.prototipospots.models.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt

const val EXTRA_SPOT_INFO_TITLE = "EXTRA_SPOT_INFO_TITLE"
const val EXTRA_CREATE_TITLE = "EXTRA_CREATE_TITLE"
const val REQUEST_CODE = 1234
const val EXTRA_LAT = "EXTRA_LAT"
const val EXTRA_LNG = "EXTRA_LNG"
private const val TAG = "MapsActivity"
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var infoShown: Boolean = true
    private var actualPlace: Place? = null
    private var lastMarkerPosition = LatLng(-0.00, -0.00)
    private lateinit var mMap: GoogleMap
    private var markers: MutableList<Marker> = mutableListOf()
    private lateinit var binding: ActivityMapsBinding
    private val places: MutableList<Place> = mutableListOf()
    private val df = DecimalFormat("#.####")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.view?.let {
            Snackbar.make(it, "Long press to add a marker!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok") {}
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
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
        //val  spots : MutableList<Spot> = mutableListOf()

        //Carga puntos en una lista mutable
        loadSpots(mMap)
        //llena el mapa con los puntos cargados
        loadMarkers()
        mMap.setOnMapLongClickListener {  latLng ->
            Log.i(TAG,"setOnMapLongClickListener")
            crearNuevoSpot(latLng)

        }


        mMap.setOnMarkerClickListener { marker ->

            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position),600,null)
            when {
                (lastMarkerPosition == marker.position && infoShown) -> {//V : V = F
                    Log.i(TAG, "hide same point")
                    marker.hideInfoWindow()
                    infoShown = !infoShown
                }
                lastMarkerPosition == marker.position && !infoShown -> {
                    Log.i(TAG, "show same point")
                    marker.showInfoWindow()
                    infoShown = !infoShown
                }
                lastMarkerPosition != marker.position && infoShown -> {//F : V = V
                    Log.i(TAG, "show different point, infoShown")
                    marker.showInfoWindow()
                    lastMarkerPosition = marker.position
                }
                else -> {//F : F = V
                    Log.i(TAG, "show different point, Not InfoShown")
                    marker.showInfoWindow()
                    lastMarkerPosition = marker.position
                    infoShown = !infoShown
                }
            }

            true
        }


        mMap.setOnInfoWindowClickListener { markerInfo ->
            Log.i(TAG, "OnInfoWindowClickListener")

            getPlaceFromPlaces(markerInfo)
        }


        // Add a marker in Sydney and move the camera
        val santiago = LatLng(-33.45694, -70.64827)
        //mMap.addMarker(MarkerOptions().position(santiago).title("Santiago"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(santiago, 10f), 500, null)

    }

    private fun closeToMarker(latLng: LatLng): Boolean {
        for (place in places) {
            val dist = df.format(calculaDist(latLng, place.latitude, place.longitude)).toDouble()
            Log.i(TAG, "dist = $dist to ${place.description}")
            if (dist < 0.0122) {
                actualPlace = place
                return true
            }
        }
        return false
    }

    private fun calculaDist(latLng: LatLng, x2: Double, y2: Double): Any {
        val x1 = latLng.latitude
        val y1 = latLng.longitude
        val restX = (x2 - x1).pow(2)
        val restY = (y2 - y1).pow(2)
        return sqrt(restX + restY)
    }

    private fun loadMarkers() {
        for (place in places){
            val lat = place.latitude
            val lng = place.longitude
            val latLng = LatLng(lat,lng)

            mMap.addMarker(MarkerOptions().title(place.title).position(latLng).snippet(place.description))
        }
    }


    private fun getPlaceFromPlaces(markerInfo: Marker) {
        Log.i(TAG,"places is empty = ${places.isEmpty()}")
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
                actualPlace=place
                spotsRef.whereEqualTo("id",place.id)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result){
                            val intent = Intent(this@MapsActivity, DisplaySpotInfoActivity::class.java)

                            intent.putExtra(EXTRA_PLACE,actualPlace)
                            intent.putExtra(EXTRA_SPOT_INFO_TITLE, "${place.title}")
                            startActivity(intent)
                        }
                    }

            }

        }
    }


    private fun crearNuevoSpot(latLng: LatLng) {
        Log.i(TAG, "VALOR coordenadas: $latLng")

        if(!closeToMarker(latLng)) {
            val dialog =
                AlertDialog.Builder(this)
                    .setTitle("Crear punto de observación")
                    .setMessage(
                        "Quiere crear un punto de observación en las coordenadas:\n" +
                                "Latitud: ${df.format(latLng.latitude)}\nLongitud: ${
                                    df.format(
                                        latLng.longitude
                                    )
                                }"
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Ok", null)
                    .show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val intent = Intent(this@MapsActivity, CreateMarkerActivity::class.java)
                val lat = latLng.latitude
                val lng = latLng.longitude
                intent.putExtra(EXTRA_LAT, lat)
                intent.putExtra(EXTRA_LNG, lng)
                intent.putExtra(EXTRA_CREATE_TITLE, "Crear punto de observación")
                startActivityForResult(intent, REQUEST_CODE)
                dialog.dismiss()
            }
        }else{
            Log.i(TAG,"actual place = ${actualPlace?.description}")

            val dialog =
                AlertDialog.Builder(this)
                    .setTitle("CUIDADO")
                    .setMessage("El punto que intenta crear esta muy cerca de otro punto\n" +
                            "Desea crear un punto de todas maneras?")
                    .setPositiveButton("Sí",null)
                    .setNegativeButton("Cancelar",null)
                    .show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val intent = Intent(this@MapsActivity, CreateMarkerActivity::class.java)
                val lat = latLng.latitude
                val lng = latLng.longitude
                intent.putExtra(EXTRA_LAT, lat)
                intent.putExtra(EXTRA_LNG, lng)
                intent.putExtra(EXTRA_CREATE_TITLE, "Crear punto de observación")
                startActivityForResult(intent, REQUEST_CODE)
                dialog.dismiss()
            }

        }
    }

    private fun loadSpots(mMap: GoogleMap) {
        val db = Firebase.firestore
        var cont = 0
        val spotsCollection = db.collection("spots")
        spotsCollection
            .get()
            .addOnSuccessListener { result ->
                Log.i(TAG, "SE PUDO LEER DE LA BASE DE DATOS")
                for (document in result) {
                    Log.i(TAG, "se lee spot como ${document.id}, Pos = $cont")
                    val docData = document.data
                    val placeDescript = docData["description"] as String?
                    val placeName = docData["name"] as String
                    val placeLat = docData["lat"] as Double
                    val placeLng = docData["lng"] as Double
                    val placeId = docData["id"] as String
                    val place = Place(placeId,placeName, placeDescript, placeLat, placeLng)
                    places.add(cont,place)
                    cont+=1
                }
                for (place in places) {

                    val lat = place.latitude
                    val lng = place.longitude
                    val latLng = LatLng(lat, lng)
                    mMap.addMarker(
                        MarkerOptions().title(place.title).position(latLng)
                            .snippet(place.description)
                    )
                }

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //Get new map data from the data
            val newSpot = data?.getSerializableExtra(EXTRA_PLACE) as Place
            val latLng = LatLng(newSpot.latitude,newSpot.longitude)
            places.add(newSpot)
            mMap.addMarker(MarkerOptions().title(newSpot.title).position(latLng).snippet(newSpot.description))
            Log.i(TAG, "onActivityResult with new map title ${newSpot.title}")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}