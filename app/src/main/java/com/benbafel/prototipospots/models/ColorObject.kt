package com.benbafel.prototipospots.models

class ColorObject(var name: String,  hex: String, contrastHex: String) {
    val hexHash: String = "#$hex"
    val contrastHexHash:String = "#$contrastHex"
}