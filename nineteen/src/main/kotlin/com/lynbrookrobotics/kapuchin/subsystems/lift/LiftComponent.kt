package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*

class LiftComponent(hardware: LiftHardware) : Component<LiftComponent, LiftHardware, OffloadedOutput>(hardware, EventLoop) {

    val cargoCollect by pref(8.5, Inch)
    val panelCollect by pref(1.5, Inch)
    val panelCollectStroke by pref(9.75, Inch)
    val collectGroundPanel by pref(0, Inch)

    val panelLowRocket by pref(4.24, Inch)
    val rocketLevelShift by pref(29, Inch)
    val panelMidRocket get() = panelLowRocket + rocketLevelShift
    val panelHighRocket get() = panelLowRocket + rocketLevelShift * 2

    private val panelCargoOffset by pref(-2.5, Inch)
    val cargoLowRocket get() = panelLowRocket + panelCargoOffset
    val cargoCargoShip by pref(17, Inch)
    val cargoMidRocket get() = panelMidRocket + panelCargoOffset
    val cargoHighRocket get() = panelHighRocket + panelCargoOffset

    val positionGains by pref {
        val kP by pref(12, Volt, 12, Inch)
        val kD by pref(0, Volt, 2, FootPerSecond)
        ({
            OffloadedEscGains(
                    syncThreshold = hardware.syncThreshold,
                    kP = hardware.conversions.native.native(kP),
                    kD = hardware.conversions.native.native(kD)
            )
        })
    }

    override val fallbackController: LiftComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    override fun LiftHardware.output(value: OffloadedOutput) {
        value.with(hardware.conversions.safeties).writeTo(esc)
    }
}
