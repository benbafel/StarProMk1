package com.benbafel.prototipospots

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.HeaderViewListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.benbafel.prototipospots.databinding.ActivityDisplaySpotInfoBinding
import com.benbafel.prototipospots.models.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_display_spot_info.*
import kotlinx.coroutines.delay
import java.text.DecimalFormat


private const val TAG = "DisplaySpotInfoActivity"
const val EXTRA_COMM_LIST = "EXTRA_COMM_LIST"
class DisplaySpotInfoActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityDisplaySpotInfoBinding
    private lateinit var vMap : MapView
    private lateinit var place: Place
    private lateinit var spot: Spot
    private lateinit var user: User
    private  var commList = mutableListOf<Comment>()
    private val df = DecimalFormat("#.####")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDisplaySpotInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        vMap = spotInfoMap
        vMap.getMapAsync(this)
        vMap.onCreate(savedInstanceState)
        place = intent.getSerializableExtra(EXTRA_PLACE) as Place
        user = intent.getSerializableExtra(EXTRA_USER) as User
        Log.i(TAG,"USER MAIL = ${user.email}")
        supportActionBar?.title = intent.getStringExtra(EXTRA_SPOT_INFO_TITLE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        btnReport.setOnClickListener{
            report()
        }
        btnUsersComments.setOnClickListener{
            if(commList.isNotEmpty()){
                Log.i(TAG,"LISTA NO ESTA VACIA")
                val cList = CommentList(commList)
                val intent = Intent(this,DisplayCommentList::class.java)
                intent.putExtra(EXTRA_COMM_LIST,cList)
                startActivity(intent)
            }else
                Toast.makeText(
                    this,
                    "No hay comentarios en este punto",
                    Toast.LENGTH_SHORT
                ).show()
        }


    }

    private fun report() {
        val alertDialog = AlertDialog
            .Builder(this)
            .setTitle("Reportar punto")
            .setMessage("Quieres reportar este punto?")
            .setPositiveButton("Si",null)
            .setNegativeButton("No",null)
            .show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{
            val mapFormView = LayoutInflater.from(this).inflate(R.layout.dialog_report_spot,null)
            val alertdialog2 =
                AlertDialog.Builder(this)
                    .setTitle("Reporte")
                    .setView(mapFormView)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Ok",null)
                    .show()
            alertdialog2.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{
                val title = mapFormView.findViewById<EditText>(R.id.etReport).text.toString()
                if(title.trim().isEmpty()) {
                    Toast.makeText(this, "el reporte debe contener algo!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                val alertDialog3 = AlertDialog
                    .Builder(this)
                    .setTitle("Reporte enviado")
                    .setMessage("Su reporte ha sido enviado para revision.\n" +
                            "Muchas Gracias!")
                    .setPositiveButton("OK",null)
                    .show()
                alertDialog3.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{
                    alertDialog.dismiss()
                    alertdialog2.dismiss()
                    alertDialog3.dismiss()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.i(TAG,"se creo la opcion return")
        return super.onCreateOptionsMenu(menu)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        val db = Firebase.firestore
        val id = place.id
        getComments(id,db)
        val lat = place.latitude
        val lng = place.longitude
        val latLng = LatLng(lat,lng)
        val dfLat = df.format(lat)
        val dfLng = df.format(lng)
        getSpotInfo(db,id)



        setMapRestrictions(googleMap)
        loadInfoFromPlace(googleMap,latLng)
        tvCoordenadas.text = resources.getString(R.string.coordPlace,dfLat,dfLng)
        tvSpotName.text = place.title
        tvUserComment.text = place.description


    }

    private fun getSpotInfo(db: FirebaseFirestore, id: String) {
        db.collection("spots").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.i(TAG, " log 1 = ${document.id} => ${document.data}")
                    llenarSpot(document.data)
                } else {
                    Log.i(TAG, "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val myIntent = Intent(applicationContext, MapsActivity::class.java)
        myIntent.putExtra(EXTRA_USER_MAIL,user.email)
        startActivityForResult(myIntent, 0)
        return true
    }
    private fun llenarSpot(data: Map<String, Any>?)  {
        val id = place.id
        val name = place.title
        val description =  place.description
        val createdBy =  data!!["createdBy"] as String?
        Log.i(TAG,"createdBy = $createdBy")
        val lat =  place.latitude
        val lng = place.longitude
        val bortleCenter = (data["bortleCenter"] as Long).toInt()
        Log.i(TAG,"bortleCenter = $bortleCenter")
        val maxBortle = (data["maxBortle"] as Long).toInt()
        Log.i(TAG,"maxBortle = $maxBortle")
        val accesibility = (data["accesibility"] as Long).toInt()
        Log.i(TAG,"accesibility = $accesibility")
        val spotQuality = (data["spotQuality"] as Double).toFloat()
        Log.i(TAG,"spotQuality = $spotQuality")
        spot = Spot(id,name,description,createdBy,lat,lng,bortleCenter,maxBortle,accesibility,spotQuality)
        rbQual.rating = spotQuality
        insertBortleValViews(bortleCenter,maxBortle)
        fillAccessibilityView(accesibility,tvAccessibility)

        return
    }

    private fun getComments(id: String, db: FirebaseFirestore) {
        db.collection("spots")
            .document(id)
            .collection("spotComments")
            .get()
            .addOnSuccessListener { results ->
                for(document in results){
                    val docData = document.data
                    val comId = docData["id"] as String
                    val comUser = docData["userName"] as String
                    val comComment  = docData["comment"] as String
                    val comment = Comment(comId,comUser,comComment)
                    commList.add(comment)
                }
                Log.i(TAG,"LIST SIZE = ${commList.size}")
            }.addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun fillAccessibilityView(accesibility: Int, txtView: TextView?) {
        when (accesibility) {
            1 -> txtView!!.text = resources.getString(R.string.Muy_Facil)
            2 -> txtView!!.text = resources.getString(R.string.Facil)
            3 -> txtView!!.text = resources.getString(R.string.Normal)
            4 -> txtView!!.text = resources.getString(R.string.Dificil)
            5 -> txtView!!.text = resources.getString(R.string.Muy_Dificil)
        }
    }

    private fun insertBortleValViews(bortleCenter: Int, maxBortle: Int) {
        fillBortle(bortleCenter,tvBortleCenter)
        fillBortle(maxBortle,tvBortleArea)

    }

    private fun fillBortle(bortle: Int, txtView: TextView?) {
        when (bortle) {
            1 -> txtView!!.text = resources.getString(R.string.b1)
            2 -> txtView!!.text = resources.getString(R.string.b2)
            3 -> txtView!!.text = resources.getString(R.string.b3)
            4 -> txtView!!.text = resources.getString(R.string.b4)
            5 -> txtView!!.text = resources.getString(R.string.b5)
            6 -> txtView!!.text = resources.getString(R.string.b6)
            7 -> txtView!!.text = resources.getString(R.string.b7)
            8 -> txtView!!.text = resources.getString(R.string.b8)
            9 -> txtView!!.text = resources.getString(R.string.b9)
        }
    }


    private fun setMapRestrictions(googleMap: GoogleMap) {
        googleMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = false
        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = true

    }

    private fun loadInfoFromPlace(googleMap: GoogleMap,latLng:LatLng) {
        googleMap.addMarker((
                MarkerOptions().title(place.title).position(latLng)
                    .snippet(place.description)
                ))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
    }

    @Override
    override fun onStart() {
        super.onStart()
        vMap.onStart()
    }

    @Override
    override fun onResume() {
        super.onResume()
        vMap.onResume()
    }

    @Override
    override fun onPause() {
        super.onPause()
        vMap.onPause()
    }

    @Override
    override fun onStop() {
        super.onStop()
        vMap.onStop()
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
        vMap.onDestroy()
    }

    @Override
    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        vMap.onSaveInstanceState(outState)
    }

    @Override
    override fun onLowMemory() {
        super.onLowMemory()
        vMap.onLowMemory()
    }
}