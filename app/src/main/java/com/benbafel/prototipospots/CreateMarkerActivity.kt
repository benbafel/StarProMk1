package com.benbafel.prototipospots

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import com.benbafel.prototipospots.databinding.ActivityCreateMarkerBinding
import com.benbafel.prototipospots.models.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_marker.*
import java.util.*

const val EXTRA_PLACE = "EXTRA_SPOT"
private const val TAG = "CreateMarkerActivity"
@Suppress("DEPRECATION")
class CreateMarkerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateMarkerBinding
    lateinit var selectedBortleCenter: ColorObject
    lateinit var selectedBortleArea : ColorObject
    lateinit var selectedAccessibility: ColorObject
    private var spotQlty: Float = 0f
    var positionCenter :Int? = null
    var positionArea :Int? = null
    var positionAccessibility :Int? = null
    var selectedBortleCenterName: String? = null
    var selectedAccessibilityName: String? = null
    var selectedBortleAreaName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMarkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = intent.getStringExtra(EXTRA_CREATE_TITLE)
        
        loadView()

        Log.i(TAG, " Intent Crear punto")
        Log.i(TAG, "${intent.getDoubleExtra(EXTRA_LAT,0.00)},${intent.getDoubleExtra(EXTRA_LNG,0.00)}")



        btnAddMrk.setOnClickListener {

            saveSpot()

        }
    }

    private fun loadView() {
        loadAccessibilitySpinner()
        loadBortleCenterSpinner()
        loadBortleAreaSpinner()
    }

    private fun saveSpot() {
        when {
            viewsRfull() -> {
                var dialog =
                    AlertDialog.Builder(this)
                        .setTitle("Crear nuevo punto de observación")
                        .setMessage("Estas seguro que deseas crear un punto nuevo?")
                        .setPositiveButton("Estoy seguro", null)
                        .setNegativeButton("Cancelar",null)
                        .show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val db = Firebase.firestore
                    val spotId = UUID.randomUUID().toString()
                    val spotName = "punto de prueba"
                    val descChar = etCommentary.text as CharSequence
                    val description = removeNewLine(descChar).toString()
                    val spotUser = "benbafel"
                    val lat = intent.getDoubleExtra(EXTRA_LAT,0.00)
                    val lng = intent.getDoubleExtra(EXTRA_LNG,0.00)
                    val bortleCenterSpot = positionCenter
                    val borleAreaSpot = positionArea
                    val accessibilitySpot = positionAccessibility
                    val spotQuality = setSpotQuality(bortleCenterSpot,borleAreaSpot)
                    val spot = Spot(
                        spotId,
                        spotName,
                        description,
                        spotUser,
                        lat,
                        lng,
                        bortleCenterSpot,
                        borleAreaSpot,
                        accessibilitySpot,
                        spotQuality)
                    //Check if spot exists
                    db.collection("spots").document(spotId)
                        .set(spot)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                            dialog.dismiss()
                            dialog =
                                AlertDialog.Builder(this)
                                    .setTitle("Punto creado")
                                    .setMessage("Se ha creado el punto exitosamente")
                                    .setPositiveButton("Ok", null)
                                    .show()
                            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                                val data = Intent()
                                val place = Place(spotId,spot.name,spot.description,spot.lat,spot.lng)
                                data.putExtra(EXTRA_PLACE,place)
                                setResult(Activity.RESULT_OK,data)
                                finish()
                            }

                        }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                }

            }
            else -> {
                val dialog2 =
                    AlertDialog.Builder(this)
                        .setMessage("FALTAN VISTAS POR LLENAR")
                        .setPositiveButton("Ok", null)
                        .show()
                dialog2.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    dialog2.dismiss()
                }
            }
        }

    }

    private fun removeNewLine(descChar: CharSequence): CharSequence {
        var descChar2 = descChar
        while(descChar2.endsWith(" ") || descChar2.endsWith("\n") ) {
            descChar2 = descChar2.dropLast(1)
        }
        return descChar2
    }

    private fun viewsRfull(): Boolean {
        return (positionAccessibility != null
                && positionCenter != null
                && positionArea != null
                && etCommentary.text.trim().isNotEmpty())
        }

    private fun loadAccessibilitySpinner() {
         selectedAccessibility = ListaColor().defaultColorAccessibility
         binding.spnrAccessibility.apply {
             adapter = ColorSpinnerAdapter(applicationContext, ListaColor().accessibilityList())
             setSelection(ListaColor().posicionEnListaAccessibility(selectedAccessibility), false)
             onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                 override fun onItemSelected(parent: AdapterView<*>?,
                                            view: View?,
                                            position: Int,
                                            id: Long) {
                     selectedAccessibility = ListaColor().accessibilityList()[position]
                     positionAccessibility = position
                     selectedAccessibility.name.also { selectedAccessibilityName = it }
                 }

                 override fun onNothingSelected(parent: AdapterView<*>?) {}
             }
         }
     }

    private fun loadBortleCenterSpinner() {
        selectedBortleCenter = ListaColor().defaultColorBortle
        binding.spnrBortleCenter.apply {
            adapter = ColorSpinnerAdapter(applicationContext,ListaColor().bortleList())
            setSelection(ListaColor().posicionEnListaBortle(selectedBortleCenter),false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onItemSelected(parent: AdapterView<*>?,
                                            view: View?,
                                            position: Int,
                                            id: Long) {
                    selectedBortleCenter = ListaColor().bortleList()[position]
                    positionCenter = position
                    selectedBortleCenterName = selectedBortleCenter.name
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun loadBortleAreaSpinner() {
        selectedBortleArea = ListaColor().defaultColorBortle
        binding.spnrBortleArea.apply {
            adapter = ColorSpinnerAdapter(applicationContext,ListaColor().bortleList() )
            setSelection(ListaColor().posicionEnListaBortle(selectedBortleArea),false)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

                override fun onItemSelected(parent: AdapterView<*>?,
                                            view: View?,
                                            position: Int,
                                            id: Long) {
                    selectedBortleArea = ListaColor().bortleList()[position]
                    positionArea = position
                    selectedBortleAreaName = selectedBortleArea.name
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setSpotQuality(bortleCenter: Int?,maxBortle: Int?): Float {
         when {
            bortleCenter!! <= 2 && maxBortle!! <= 2 -> {
                spotQlty = 5.85935f
                return spotQlty
            }
            bortleCenter == 3 && maxBortle == 3 -> {
                spotQlty = 4f
                return spotQlty
            }
            bortleCenter in 4..5 && maxBortle in 4..5 -> {
                spotQlty = 3f
                return spotQlty
            }
            bortleCenter in 6..7 && maxBortle in 6..7 -> {
                spotQlty = 2f
                return spotQlty
            }
            bortleCenter in 8..9 && maxBortle in 8..9 -> {
                spotQlty = 1f
                return spotQlty
            }
             else -> {
                 return 0f
             }
        }
    }
    /* private fun fillLatLng() {
         val latLng=intent.getSerializableExtra() as LatLng
         val text = "Coordenadas:Latitud:${latLng.latitude}Longitud=${latLng.longitude}"

     }*/

}




