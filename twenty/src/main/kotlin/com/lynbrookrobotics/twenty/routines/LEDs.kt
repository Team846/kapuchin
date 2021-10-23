package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.subsystems.driver.LedComponent
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import java.awt.Color

suspend fun LedComponent.set(color: Color) = startRoutine("Set") {
    controller { color }
}

suspend fun LedComponent.rainbow(period: Time) = startRoutine("Rainbow") {
    controller { Color(Color.HSBtoRGB(((currentTime.Second / period.Second % 1.0)).toFloat(), 1f, 1f)) }
}

suspend fun LedComponent.blink(color: Color, period: Time = 100.milli(Second)) = startRoutine("Blink") {
    var on = true
    var lastTime = currentTime

    controller {
        if (currentTime - lastTime > period) {
            lastTime = currentTime
            on = !on
        }

        if (on) color else Color.BLACK
    }
}