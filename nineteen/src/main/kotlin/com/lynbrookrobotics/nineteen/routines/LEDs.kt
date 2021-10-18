package com.lynbrookrobotics.nineteen.routines

import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.nineteen.subsystems.driver.LedComponent
import info.kunalsheth.units.generated.*
import java.awt.Color

suspend fun LedComponent.set(color: Color) = startRoutine("Set") {
    controller { color }
}

suspend fun LedComponent.rainbow() = startRoutine("Rainbow") {
    controller { Color(Color.HSBtoRGB(((currentTime.Second / periods.first.Second % 1.0)).toFloat(), 1f, 1f)) }
}

//suspend fun LedComponent.fade(period: Time, vararg colors: Color) = startRoutine("Fade") {
//
//    val diffList = mutableListOf<Triple<Double, Double, Double>>()
//    val size = colors.size
//    val freq = (period / hardware.period).Each.toInt()
//
//    var cur = colors[0]
//    var count = 0
//    var index = 0
//
//    for (i in 0 until size) {
//        diffList.add(
//                Triple(
//                        ((colors[(i + 1) % size].red - colors[i].red) / freq).toDouble(),
//                        ((colors[(i + 1) % size].green - colors[i].green) / freq).toDouble(),
//                        ((colors[(i + 1) % size].blue - colors[i].blue) / freq).toDouble()
//                )
//        )
//    }
//
//    val diff = diffList.toTypedArray()
//
//    controller {
//        if (count == freq) {
//            count = 0
//            index++
//        }
//
//        count++
//
//        cur.also {
//            cur = Color(
//                    cur.red + diff[index % size].first.toInt(),
//                    cur.green + diff[index % size].second.toInt(),
//                    cur.blue + diff[index % size].third.toInt()
//            )
//        }
//    }
//}