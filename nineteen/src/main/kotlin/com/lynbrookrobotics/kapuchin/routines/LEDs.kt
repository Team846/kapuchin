//package com.lynbrookrobotics.kapuchin.routines
//
//import com.lynbrookrobotics.kapuchin.subsystems.driver.*
//import com.lynbrookrobotics.kapuchin.timing.*
//import info.kunalsheth.units.generated.*
//import info.kunalsheth.units.math.*
//import java.awt.Color
//import kotlin.math.PI
//import kotlin.math.abs
//import kotlin.math.sin
//
//suspend fun LEDLightsComponent.rainbow(period: Time) = startRoutine("Rainbow") {
//
//    val startTime = currentTime
//
//    controller {
//        Color(Color.HSBtoRGB(((currentTime.Second / 5 % 1.0)).toFloat(), 1f, 1f)).takeIf { currentTime - startTime < period }
//    }
//}
//
//suspend fun LEDLightsComponent.cycle(count: Int, period: Time, vararg colors: Color) = startRoutine("Cycle") {
//
//    var index = 0
//    var prevTime: Time = 0.milli(Second)
//    var color: Color? = null
//
//    controller {
//        if (currentTime - prevTime > period) {
//            color = colors[index % colors.size]
//            index++
//            prevTime = currentTime
//        }
//
//        color.takeIf { index <= count }
//    }
//}
//
//suspend fun LEDLightsComponent.fade(period: Time, color: Color) = startRoutine("Fade") {
//
//    val hue = Color.RGBtoHSB(color.red, color.green, color.blue, null)[0]
//
//    controller {
//        Color(Color.HSBtoRGB(hue, 1.0f, (abs(sin(((currentTime.Second / period.Second) % 1.0) * PI))).toFloat()))
//    }
//}