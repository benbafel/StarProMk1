package com.benbafel.prototipospots.models
import java.io.Serializable
data class Spot (var name: String?,
                 var description: String?,
                 var createdBy: String?,
                 val latitude: Double,
                 val longitude: Double,
                 var bortleCenter: Int,
                 var maxBortle: Int,
                 var accesibility: String?,
                 var spotQuality: Int
                 //route:
                 ) :Serializable{

    private fun setSpotQuality(bortleCenter: Int,maxBortle: Int) {
        when{
            bortleCenter <= 2 && maxBortle <= 2 ->{
               this.spotQuality = 5
            }
            bortleCenter == 3 && maxBortle == 3 ->{
                this.spotQuality = 4
            }
            bortleCenter in 4..5 && maxBortle in 4..5 ->{
                this.spotQuality = 3
            }
            bortleCenter in 6..7 && maxBortle in 6..7 ->{
                this.spotQuality = 2
            }
            bortleCenter in 8..9 && maxBortle in 8..9 ->{
                this.spotQuality = 1
            }
        }
    }

}







