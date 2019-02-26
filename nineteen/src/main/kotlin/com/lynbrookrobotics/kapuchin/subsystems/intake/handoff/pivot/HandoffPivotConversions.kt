package com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot

import com.lynbrookrobotics.kapuchin.control.conversion.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*

class HandoffPivotConversions(val hardware: HandoffPivotHardware) : Named by Named("Conversions", hardware) {

    private val conversions by pref {
        val min by pref {
            val real by pref(0, Degree)
            val native by pref(26)
            ({ real to native })
        }

        val max by pref {
            val real by pref(90, Degree)
            val native by pref(194)
            ({ real to native })
        }

        val zeroOffset by pref(13.928, Degree)

        ({
            val nfu = max.second - min.second
            val pfq = max.first - min.first

            AngularOffloadedNativeConversion(::div, ::div, ::times, ::times,
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