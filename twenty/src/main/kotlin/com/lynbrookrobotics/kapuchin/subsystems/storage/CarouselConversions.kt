package com.lynbrookrobotics.kapuchin.subsystems.storage

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.roundToInt
import kotlin.math.roundToInt

class CarouselConversions(hardware: CarouselHardware) : Named by Named("Conversions", hardware) {

    val rubberWheel by pref(1.0, Inch)
    val bigWheel by pref(10.0, Inch)

    val conversionFactor = rubberWheel / bigWheel
}