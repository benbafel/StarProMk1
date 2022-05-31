package com.benbafel.prototipospots

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.benbafel.prototipospots.databinding.ActivityCreateMarkerBinding
import com.benbafel.prototipospots.models.ColorSpinnerAdapter
import com.benbafel.prototipospots.models.ListaColor
import com.benbafel.prototipospots.models.ColorObject
import com.benbafel.prototipospots.models.Spot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.FirestoreGrpc
import com.google.type.LatLng
import kotlinx.android.synthetic.main.activity_create_marker.*
import java.text.DecimalFormat


private val df = DecimalFormat("#.#####")
private const val TAG = "CreateMarkerActivity"
@Suppress("DEPRECATION")
class CreateMarkerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateMarkerBinding
    lateinit var selectedBortleCenter: ColorObject
    lateinit var selectedBortleArea : ColorObject
    lateinit var selectedAccessibility: ColorObject
    var positionCenter :Int? = null
    var positionArea :Int? = null
    var positionAccessibility :Int? = null
    var selectedBortleCenterName: String? = null
    var selectedAccessibilityName: String? = null
    var selectedBortleAreaName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_marker)
        binding = ActivityCreateMarkerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        loadView()


        Log.i(TAG, " Intent Crear punto")
        Log.i(TAG, "${intent.getStringExtra(EXTRA_LAT)},${intent.getStringExtra(EXTRA_LNG)}")



        supportActionBar?.title = intent.getStringExtra(EXTRA_CREATE_TITLE)

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
                val dialog =
                    AlertDialog.Builder(this)
                        .setMessage("todas las vistas llenas")
                        .setPositiveButton("Ok", null)
                        .show()
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    dialog.dismiss()
                }
            }
            else -> {
                Log.i(TAG, "S")
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

    private fun viewsRfull(): Boolean {
        return (positionAccessibility != null
                && positionCenter != null
                && positionArea != null
                && etCommentary.text.isNotEmpty())
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
    /* private fun fillLatLng() {
         val latLng=intent.getSerializableExtra() as LatLng
         val text = "Coordenadas:Latitud:${latLng.latitude}Longitud=${latLng.longitude}"

     }*/

}




