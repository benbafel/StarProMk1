package com.benbafel.prototipospots

import android.app.Activity
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.benbafel.prototipospots.databinding.ActivityCreateMarkerBinding
import com.benbafel.prototipospots.models.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create_marker.*
import java.text.DecimalFormat
import java.util.*

const val EXTRA_PLACE = "EXTRA_PLACE"
const val EXTRA_MODIFIED_PLACE = "EXTRA_MODIFIED_PLACE"
private const val TAG = "CreateMarkerActivity"
@Suppress("DEPRECATION")
class CreateMarkerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateMarkerBinding
    private lateinit var spot: Spot
    lateinit var selectedBortleCenter: ColorObject
    lateinit var selectedBortleArea : ColorObject
    lateinit var selectedAccessibility: ColorObject
    private lateinit var user: User
    private var spotQlty: Float = 0f
    private var parent = -1
    var positionCenter :Int? = null
    var positionArea :Int? = null
    var positionAccessibility :Int? = null
    var selectedBortleCenterName: String? = null
    var selectedAccessibilityName: String? = null
    var selectedBortleAreaName: String? = null
    private val df = DecimalFormat("#.####")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMarkerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = intent.getStringExtra(EXTRA_CREATE_TITLE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        parent = intent.getIntExtra(PARENT,-1)
        user = intent.getSerializableExtra(EXTRA_USER) as User
        if(parent == 2){
            spot = intent.getSerializableExtra(EXTRA_MODIFY_SPOT) as Spot
            Log.i(TAG, "se cargo la wea de spot.")
        }
        Log.i(TAG,"user: $user")
        loadView()


        Log.i(TAG, "coordenadas ${intent.getDoubleExtra(EXTRA_LAT,0.00)},${intent.getDoubleExtra(EXTRA_LNG,0.00)}")

        ibBortle.setOnClickListener{
            showBortleExplain()
        }
        ibAccessibily.setOnClickListener{
            showAccesibilityExplain()
        }

        btnAddMrk.setOnClickListener {

            saveSpot()

        }
    }

    private fun showAccesibilityExplain() {
        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Accesibilidad")
                .setMessage(getString(R.string.access_explain))
                .setPositiveButton("Ok",null)
                .show()

    }

    private fun showBortleExplain() {
        val lat = intent.getDoubleExtra(EXTRA_LAT,0.00)
        val lng = intent.getDoubleExtra(EXTRA_LNG,0.00)
        val latLngString = "$lat,$lng"

        val dialogFormView = LayoutInflater.from(this).inflate(R.layout.bortle_explain_1,null)
        val dialog =
            AlertDialog.Builder(this)
                .setView(dialogFormView)
                .setNegativeButton("Coordenadas",null)
                .setPositiveButton("Aceptar",null)
                .show()
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Texto Copiado",latLngString)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this,"Texto Copiado",Toast.LENGTH_LONG).show()
        }

    }

    private fun loadView() {
        loadLatLng()
        loadAccessibilitySpinner()
        loadBortleCenterSpinner()
        loadBortleAreaSpinner()
        if(parent == 2){
            val spotName = spot.name
            val spotComment = spot.description
            etSpotName.setText(spotName)
            etCommentary.setText(spotComment)
            btnAddMrk.text = getString(R.string.modify_mark_button)
        }
    }

    private fun loadLatLng() {
        val lat = df.format(intent.getDoubleExtra(EXTRA_LAT,0.00))
        val lng = df.format(intent.getDoubleExtra(EXTRA_LNG,0.00))
        val latLngString = "coordenadas: $lat,$lng"
        tvLatLng.text = latLngString
    }

    private fun saveSpot() {
        when {
            viewsRfull() -> {
                when(parent){
                    1 ->{
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
                            val spotName = etSpotName.text.toString()
                            val descChar = etCommentary.text as CharSequence
                            val description = removeNewLine(descChar).toString()
                            val spotUser = user.name
                            val lat = intent.getDoubleExtra(EXTRA_LAT,0.00)
                            val lng = intent.getDoubleExtra(EXTRA_LNG,0.00)
                            val bortleCenterSpot = positionCenter
                            val borleAreaSpot = positionArea
                            val accessibilitySpot = positionAccessibility
                            val spotQuality = setSpotQuality(bortleCenterSpot,borleAreaSpot,accessibilitySpot)
                            spot = Spot(
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
                                        data.putExtra(EXTRA_USER_MAIL,user.email)
                                        data.putExtra(EXTRA_PLACE,place)
                                        setResult(Activity.RESULT_OK,data)
                                        dialog.dismiss()
                                        startActivity(intent)
                                        finish()
                                    }


                                }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                        }
                    }
                    2 ->{
                        var dialog =
                            AlertDialog.Builder(this)
                                .setTitle("Modificar punto de observación")
                                .setMessage("Estas seguro que desea modificar este punto?")
                                .setPositiveButton("Estoy seguro", null)
                                .setNegativeButton("Cancelar",null)
                                .show()
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                            val db = Firebase.firestore
                            val spotId = spot.id
                            val spotName = etSpotName.text.toString()
                            val descChar = etCommentary.text as CharSequence
                            val description = removeNewLine(descChar).toString()
                            val spotUser = user.name
                            val lat = spot.lat
                            val lng = spot.lng
                            val bortleCenterSpot = positionCenter
                            val borleAreaSpot = positionArea
                            val accessibilitySpot = positionAccessibility
                            val spotQuality = setSpotQuality(bortleCenterSpot,borleAreaSpot,accessibilitySpot)
                            val punto = Spot(
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
                            db.collection("spots").document(spotId)
                                .set(punto)
                                .addOnSuccessListener {
                                    Log.d(TAG, "DocumentSnapshot successfully overwritten!")
                                    dialog.dismiss()
                                    dialog =
                                        AlertDialog.Builder(this)
                                            .setTitle("Punto Modificado")
                                            .setMessage("El punto ha sido modificado exitosamente")
                                            .setPositiveButton("Ok", null)
                                            .show()
                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                                        val intent = Intent(this@CreateMarkerActivity,MapsActivity::class.java)
                                        val place = Place(spotId,punto.name,punto.description,punto.lat,punto.lng)
                                        intent.putExtra(EXTRA_USER_MAIL,user.email)
                                        intent.putExtra(EXTRA_MODIFIED_PLACE,place)
                                        dialog.dismiss()
                                        startActivity(intent)
                                        this.finish()
                                    }


                                }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                        }
                    }else ->{
                        Log.i(TAG,"error de ingreso")
                    }
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
                && etCommentary.text.trim().isNotEmpty()
                && etSpotName.text.trim().isNotEmpty())
        }

    private fun loadAccessibilitySpinner() {
        when(parent){
            1 ->{
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
            2 ->{

                val pos = spot.accesibility
                selectedAccessibility = ListaColor().defaultColorAccessibility

                binding.spnrAccessibility.apply {
                    adapter = ColorSpinnerAdapter(applicationContext, ListaColor().accessibilityList())
                    setSelection(pos!!)
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
            }else ->{
                Log.i(TAG,"Error setting parent")
            }
        }
     }

    private fun loadBortleCenterSpinner() {
        when(parent){
            1 ->{
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
            2 ->{
                val pos = spot.bortleCenter
                selectedBortleCenter = ListaColor().defaultColorBortle
                binding.spnrBortleCenter.apply {
                    adapter = ColorSpinnerAdapter(applicationContext,ListaColor().bortleList())
                    setSelection(pos!!)
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
            }else ->{
                Log.i(TAG,"Error setting parent")
            }
        }
    }

    private fun loadBortleAreaSpinner() {
        when(parent){
            1 ->{
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
            2 ->{
                val pos = spot.maxBortle
                selectedBortleArea = ListaColor().defaultColorBortle
                binding.spnrBortleArea.apply {
                    adapter = ColorSpinnerAdapter(applicationContext,ListaColor().bortleList() )
                    setSelection(pos!!)
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
            }else ->{
                Log.i(TAG,"Error setting parent")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }
    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        val data = Intent()
        data.putExtra(EXTRA_USER_MAIL,user.email)
        setResult(Activity.RESULT_CANCELED,data)
        finish()
    }

    private fun setSpotQuality(bortleCenter: Int?, maxBortle: Int?, accessibilitySpot: Int?): Float {

         when {
            bortleCenter!! < 2 -> {
                spotQlty = 4.9f
            }
            bortleCenter == 2 -> {
                spotQlty = 4.1f

            }
            bortleCenter in 4..5  -> {
                spotQlty = 3.1f

            }
            bortleCenter == 6 -> {
                spotQlty = 2.90f

            }
            bortleCenter == 7 -> {
                spotQlty = 2.4f
            }
             bortleCenter == 8 -> {
                 spotQlty = 1.9f
             }
             else -> {
                 return 1.1f
             }

        }
        return spotQlty
    }

}




