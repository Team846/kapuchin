package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import java.awt.Color

suspend fun LedComponent.set(color: Color) = startRoutine("Set") {
    controller { color }
}

suspend fun LedComponent.rainbow() = startRoutine("Rainbow") {
    controller {
        Color(Color.HSBtoRGB(((currentTime.Second / 5 % 1.0)).toFloat(), 1f, 1f))
    }
}