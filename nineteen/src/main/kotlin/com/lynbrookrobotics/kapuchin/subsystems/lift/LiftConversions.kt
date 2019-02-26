package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*

class LiftConversions(val hardware: LiftHardware) : Named by Named("Conversions", hardware) {

    private val conversions by pref {
        val min by pref {
            val real by pref(12.875, Inch)
            val native by pref(275)
            ({ Pair(real, native) })
        }

        val max by pref {
            val real by pref(24, Inch)
            val native by pref(375)
            ({ Pair(real, native) })
        }

        val zeroOffset by pref(17.719, Inch)

        ({
            val nfu = max.second - min.second
            val pfq = max.first - min.first

            LinearOffloadedNativeConversion(::div, ::div, ::times, ::times,
                    nativeOutputUnits = 1023, perOutputQuantity = hardware.operatingVoltage,
                    nativeFeedbackUnits = nfu, perFeedbackQuantity = pfq,
                    feedbackZero = zeroOffset
            ) to (min to max)
        })
    }

    val native get() = conversions.first
    val minPt get() = conversions.second.first
    val maxPt get() = conversions.second.second
}