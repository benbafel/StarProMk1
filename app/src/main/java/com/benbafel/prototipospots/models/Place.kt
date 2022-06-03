package com.benbafel.prototipospots.models

import java.io.Serializable

data class Place(
    val id: String,
    val title: String?,
    val description: String?,
    val latitude: Double,
    val longitude: Double) :Serializable

        /*
        val db = Firebase.firestore
        val lat = intent.getStringExtra(EXTRA_LAT)
        val lng = intent.getStringExtra(EXTRA_LNG)
        Log.i(TAG, "tapped on save")
        if(positionAccessibility != null && positionCenter != null && positionArea != null) {
            Log.i(
                TAG,
                "position in accessibility = $positionAccessibility, nombre = $selectedAccessibilityName\n" +
                        "position in center = $positionCenter, nombre = $selectedBortleCenterName\n" +
                        "position in area = $positionArea, nombre = $selectedBortleAreaName")

            val spotsRef = db.collection("spots")
            val existeSpot = spotsRef.whereEqualTo("latLng","($lat,$lng)").limit(1)
            existeSpot.get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d(TAG, "documento ${document.id} => ${document.data}")
                        }

                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)

                }
            Log.i(TAG,"existeSpot= ")

           val dialog =
                AlertDialog.Builder(this)
                    .setTitle("Crear nuevo punto de observacion")
                    .setMessage("Está seguro que desea crear este nuevo punto de observación?")
                    .setNegativeButton("Cancel",null)
                    .setPositiveButton("Ok",null)
                    .show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener{

            }

            val spot  = Spot(
                name  = "exampleSpot",
                description =  "${etCommentary.text}",
                createdBy =  "benbafel",
                latLng =  "($lat,$lng)",
                "bortleCenter" to "$selectedBortleCenterName",
                "maxBortle" to "$selectedBortleAreaName",
                "accesibility" to "$selectedAccessibilityName",
                "spotQuality" to "buena",
            )


            db.collection("spots")
                .add(spot)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")

                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }else
            Toast.makeText(this, "Information incomplete. Please follow the form", Toast.LENGTH_LONG)
                .show()
    } */