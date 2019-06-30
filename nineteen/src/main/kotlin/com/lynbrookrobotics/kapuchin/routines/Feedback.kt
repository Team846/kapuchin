package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.feedback.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import java.awt.Color

fun Leds.set(color: Color) = newRoutine("Set") {
    controller { color }
}

fun Leds.rainbow() = newRoutine("Rainbow") {
    controller { Color(Color.HSBtoRGB(((currentTime.Second / periods.first.Second % 1.0)).toFloat(), 1f, 1f)) }
}

fun Rumble.set(rumble: TwoSided<DutyCycle>) = newRoutine("Set") {
    controller { rumble }
}

fun Rumble.trackLineFeedback(lineScanner: LineScanner, collectorSlider: CollectorSlider) = newRoutine("Track line") {

    val target by lineScanner.linePosition.readEagerly.withoutStamps
    val current by collectorSlider.hardware.position.readEagerly.withoutStamps

    controller {
        target?.takeIf { (it - current).abs < 1.5.Inch }?.let {
            TwoSided(0.Percent, 100.Percent)
        } ?: TwoSided(0.Percent)
    }
}