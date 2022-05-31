package com.benbafel.prototipospots.models

import io.grpc.okhttp.internal.framed.Header

class ListaColor {
    private val blackHex ="000000"
    private val whiteHex ="FFFFFF"

    val defaultColorBortle: ColorObject = bortleList()[0]
    val defaultColorAccessibility: ColorObject = accessibilityList()[0]

    fun posicionEnListaBortle(colorObject: ColorObject): Int{
        for (i in bortleList().indices)
        {
            if (colorObject == bortleList()[i])
                return i
        }
        return 0
    }
    fun posicionEnListaAccessibility(colorObject: ColorObject): Int{
        for (i in accessibilityList().indices)
        {
            if (colorObject == accessibilityList()[i])
                return i
        }
        return 0
    }

    fun bortleList() :List<ColorObject>{

        return listOf<ColorObject>(
            ColorObject("Elija nivel de luminosidad","799FFF",blackHex),
            ColorObject("1.Cielo oscuro excelente","FF000000",whiteHex),
            ColorObject("2.Cielo oscuro tipico","1201ff",whiteHex),
            ColorObject("3.Cielo rural","65aa00",whiteHex),
            ColorObject("4.Cielo rural-periurbano","ffea03",blackHex),
            ColorObject("5.Cielo periurbano","feae00",whiteHex),
            ColorObject("6.Cielo periurbano brillante","ff5300",whiteHex),
            ColorObject("7.Cielo periurbano-urbano","ee0f00",whiteHex),
            ColorObject("8.Cielo urbano","b20000",whiteHex),
            ColorObject("9.Cielo de centro de ciudad","dd00af",whiteHex)
        )
    }

    fun accessibilityList():List<ColorObject>{
        return listOf<ColorObject>(
            ColorObject("Elija  Accesibilidad","799FFF",blackHex),
            ColorObject("Facil","44FF00",blackHex),
            ColorObject("Normal","FFEA00",blackHex),
            ColorObject("Dificil","F46608",whiteHex),
            ColorObject("Muy Dificil","F10000",whiteHex)
        )

    }
}