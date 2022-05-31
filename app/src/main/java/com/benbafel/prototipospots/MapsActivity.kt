@file:Suppress("DEPRECATION")

package com.benbafel.prototipospots


import java.text.DecimalFormat
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.benbafel.prototipospots.databinding.ActivityMapsBinding
import com.benbafel.prototipospots.models.Spot
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val EXTRA_CREATE_TITLE = "EXTRA_CREATE_TITLE"
const val REQUEST_CODE = 1234
const val EXTRA_LAT = "EXTRA_LAT"
const val EXTRA_LNG = "LNG"
private const val TAG = "MapsActivity"
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var infoShown: Boolean = true
    private var lastMarkerPosition = LatLng(-0.00, -0.00)
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

        //val  spots : MutableList<Spot> = mutableListOf()
        //this.loadSpots()
        mMap.setOnMapLongClickListener { latLng ->
            Log.i(TAG,"setOnMapLongClickListener")
            showAlertDialog(latLng)

        }

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


        mMap.setOnInfoWindowClickListener { markerToDelete ->
            Log.i(TAG,"OnInfoWindowClickListener")
           /* markers.remove(markerToDelete)
            markerToDelete.remove() */
        }


        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }


    private fun showAlertDialog(latLng: LatLng) {
        val df = DecimalFormat("#.####")
        Log.i(TAG,"VALOR coordenadas: $latLng")
        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Crear punto de observación")
                .setMessage("Quieres crear un punto de observacion en las coordenadas:\n" +
                        "Latitud: ${df.format(latLng.latitude)}\nLongitud: ${df.format(latLng.longitude)}")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok",null)
                .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{
            val intent = Intent(this@MapsActivity,CreateMarkerActivity::class.java)
            val stringLat = latLng.latitude.toString()
            val stringLng = latLng.longitude.toString()
            intent.putExtra(EXTRA_LAT,stringLat)
            intent.putExtra(EXTRA_LNG,stringLng)
            intent.putExtra(EXTRA_CREATE_TITLE,"Crear punto de observación")
            Log.i(TAG,"$stringLat ,$stringLng")
            startActivityForResult(intent,REQUEST_CODE)
            dialog.dismiss()
        }
    }

    private fun loadSpots()  {
        val db = Firebase.firestore
        db.collection("spots")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }

            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)

            }
    }


}