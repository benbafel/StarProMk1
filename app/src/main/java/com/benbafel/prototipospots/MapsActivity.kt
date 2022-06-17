@file:Suppress("DEPRECATION")

package com.benbafel.prototipospots

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.benbafel.prototipospots.R.id.map
import com.benbafel.prototipospots.databinding.ActivityMapsBinding
import com.benbafel.prototipospots.models.Place
import com.benbafel.prototipospots.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt

const val PARENT = "PARENT"
const val EXTRA_SPOT_INFO_TITLE = "EXTRA_SPOT_INFO_TITLE"
const val EXTRA_CREATE_TITLE = "EXTRA_CREATE_TITLE"
const val REQUEST_CODE = 1234
const val DISPLAY_INFO_CODE = 3333
const val EXTRA_LAT = "EXTRA_LAT"
const val EXTRA_LNG = "EXTRA_LNG"
const val EXTRA_USER = "EXTRA_USER"
const val EXTRA_PLACE_POS = "EXTRA_PLACE_POS"
private const val TAG = "MapsActivity"
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var infoShown: Boolean = true
    private var actualPlace: Place? = null
    private var lastMarkerPosition = LatLng(-0.00, -0.00)
    private lateinit var mMap: GoogleMap
    private lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    private var markers: MutableList<Marker> = mutableListOf()
    private lateinit var binding: ActivityMapsBinding
    private val places: MutableList<Place> = mutableListOf()
    private val df = DecimalFormat("#.####")
    private lateinit var userData :User
    private lateinit var userEmail: String
    private lateinit var userLocation: LatLng
    private lateinit var markerToDisplay: Marker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.view?.let {
            Snackbar.make(it, "Mantenga presionado para añadir un marcador!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Ok") {}
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                .show()
        }

    }

    @SuppressLint("MissingPermission")
    private fun fetchLocationPermission() {
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener {
            if(it != null){
                 userLocation = LatLng(it.latitude,it.longitude)
                    Log.i(TAG,"VAL LOC: ${userLocation.toString()}")
                    mMap.addMarker(MarkerOptions().title("Ubicación del usuario").position(userLocation).snippet("Usted esta aquí (o cerca)")
                        .icon(BitmapDescriptorFactory.defaultMarker(180f)))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f))
            }else{
                mMap.addMarker(MarkerOptions().title("Ubicación predeterminada").position(userLocation).snippet("Tiene su ubicación desactivada")
                    .icon(BitmapDescriptorFactory.defaultMarker(180f)))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        userLocation = LatLng(-33.45694, -70.64827)
        fetchLocationPermission()

        userEmail = intent.getStringExtra(EXTRA_USER_MAIL) as String
        getUserData(userEmail)


        mMap = googleMap
        //val  spots : MutableList<Spot> = mutableListOf()

        //Carga puntos en una lista mutable
        loadSpots(mMap)
        //llena el mapa con los puntos cargados
        //loadMarkers()
        mMap.setOnMapLongClickListener {  latLng ->
            Log.i(TAG,"setOnMapLongClickListener")
            crearNuevoSpot(latLng)

        }


        mMap.setOnMarkerClickListener { marker ->

            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position),600,null)
            when {
                (lastMarkerPosition == marker.position && infoShown) -> {//V : V = F
                    marker.hideInfoWindow()
                    infoShown = !infoShown
                }
                lastMarkerPosition == marker.position && !infoShown -> {
                    marker.showInfoWindow()
                    infoShown = !infoShown
                }
                lastMarkerPosition != marker.position && infoShown -> {//F : V = V
                    marker.showInfoWindow()
                    lastMarkerPosition = marker.position
                }
                else -> {//F : F = V
                    marker.showInfoWindow()
                    lastMarkerPosition = marker.position
                    infoShown = !infoShown
                }
            }

            true
        }


        mMap.setOnInfoWindowClickListener { markerInfo ->
            Log.i(TAG, "OnInfoWindowClickListener")

            if(markerInfo.position != userLocation){
                getPlaceFromPlaces(markerInfo)
            }else{
                return@setOnInfoWindowClickListener
            }

        }

    }

    private fun getUserData(userEmail: String) {
        val db = Firebase.firestore
        db.collection("users").document(userEmail)
            .get()
            .addOnSuccessListener { snapshot->
                userData = setUserData(snapshot)
                Log.i(TAG,"user data loaded outside. $userData")
            }
            .addOnFailureListener{ exception ->
                Log.i(TAG, "Error getting user documents: ", exception)
            }
    }

    private fun setUserData(snapshot: DocumentSnapshot?): User {
        val docData = snapshot!!.data
        val userName = docData!!["name"] as String
        val userEmail = docData["email"] as String
        val userExpertise = docData["expertise"] as String
        val userInterests = (docData["areasOfInterest"] as Long).toInt()
        return User(userName,userEmail,userInterests,userExpertise)

    }

    private fun closeToMarker(latLng: LatLng): Boolean {
        for (place in places) {
            val dist = /*df.format(*/calculaDist(latLng, place.latitude, place.longitude)//.toDouble()
            Log.i(TAG, "dist = $dist to ${place.description}")
            if (dist < 0.0422) {
                actualPlace = place
                return true
            }
        }
        return false
    }

    private fun calculaDist(latLng: LatLng, x2: Double, y2: Double): Double {
        val x1 = latLng.latitude
        val y1 = latLng.longitude
        val restX = (x2 - x1).pow(2)
        val restY = (y2 - y1).pow(2)
        return sqrt(restX + restY)
    }

    private fun getPlaceFromPlaces(markerInfo: Marker) {
        val db = Firebase.firestore
        markerToDisplay = markerInfo
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
                Log.i(TAG,"position in places: ${places.indexOf(actualPlace)}")
                spotsRef.whereEqualTo("id",place.id)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result){
                            val intent = Intent(this@MapsActivity, DisplaySpotInfoActivity::class.java)
                            intent.putExtra(EXTRA_PLACE_POS,places.indexOf(actualPlace))
                            intent.putExtra(EXTRA_USER,userData)
                            intent.putExtra(EXTRA_PLACE,actualPlace)
                            intent.putExtra(EXTRA_SPOT_INFO_TITLE, "${place.title}")
                            startActivityForResult(intent, DISPLAY_INFO_CODE)
                        }
                    }

            }

        }
    }


    private fun crearNuevoSpot(latLng: LatLng) {
        Log.i(TAG, "VALOR coordenadas: $latLng")
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
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
                intent.putExtra(PARENT,1)
                intent.putExtra(EXTRA_USER,userData)
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
                intent.putExtra(PARENT,1)
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
                            .snippet("Clic aqui para ver datos")
                    )
                }

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            //Get new PLACE data from the data
            val newSpot = data?.getSerializableExtra(EXTRA_PLACE) as Place
            val latLng = LatLng(newSpot.latitude,newSpot.longitude)
            places.add(newSpot)
            mMap.addMarker(MarkerOptions().title(newSpot.title).position(latLng).snippet("Clic aqui para ver datos"))
            Log.i(TAG, "onActivityResult with new map title ${newSpot.title}")
        }else if(requestCode == DISPLAY_INFO_CODE && resultCode == Activity.RESULT_OK){
            val pos = data?.getIntExtra(EXTRA_PLACE_POS,-1)
            if(pos != -1){
                val placeToDelete = places[pos!!]
                Log.i(TAG,"place to delete: $placeToDelete")
                places.remove(placeToDelete)
                markerToDisplay.remove()
            } else{
                Log.i(TAG,"Error in place position, something went wrong!")
            }
        }else if(requestCode == DISPLAY_INFO_CODE && resultCode == Activity.RESULT_FIRST_USER){
            val pos = data?.getIntExtra(EXTRA_PLACE_POS,-1)
            val newPlaceData = data?.getSerializableExtra(EXTRA_PLACE) as Place
            val placeToDelete = places[pos!!]
            markerToDisplay.title = newPlaceData.title
            places.remove(placeToDelete)
            places.add(newPlaceData)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        Toast.makeText(this,"No hay donde retroceder!",Toast.LENGTH_LONG).show()
    }
}