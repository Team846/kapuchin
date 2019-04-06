package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import java.awt.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

suspend fun LedComponent.set(color: Color) = startRoutine("Set") {
    controller { color }
}

suspend fun LedComponent.rainbow() = startRoutine("Rainbow") {
    controller { Color(Color.HSBtoRGB(((currentTime.Second / 5 % 1.0)).toFloat(), 1f, 1f)) }
}

suspend fun LedComponent.fade(period: Time, color: Color) = startRoutine("Fade") {

    val hue = Color.RGBtoHSB(color.red, color.green, color.blue, null)[0]

    controller {
        Color(Color.HSBtoRGB(
                hue,
                1.0f,
                (abs(sin(((currentTime.Second / period.Second) % 1.0) * PI))).toFloat()
        ))
    }
}
