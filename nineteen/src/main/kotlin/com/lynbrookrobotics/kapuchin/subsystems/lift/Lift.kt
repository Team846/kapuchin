package com.lynbrookrobotics.kapuchin.subsystems.lift

import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import info.kunalsheth.units.generated.*

class Lift(hardware: LiftHardware) : Component<Lift, LiftHardware, OffloadedOutput>(hardware, EventLoop) {

    val cargoCollect by pref(8.5, Inch)
    val panelCollect by pref(1.5, Inch)
    val panelCollectStroke by pref(9.75, Inch)

    val rocketLevelShift by pref(29, Inch)
    val panelCargoOffset by pref(-2.5, Inch)

    val panelLow by pref(4.24, Inch)
    val panelMid get() = panelLow + rocketLevelShift
    val panelHigh get() = panelLow + rocketLevelShift * 2

    val cargoLow get() = panelLow + panelCargoOffset
    val cargoShip by pref(17, Inch)
    val cargoMid get() = panelMid + panelCargoOffset
    val cargoHigh get() = panelHigh + panelCargoOffset

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

    override val fallbackController: Lift.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, 0.Percent)
    }

    private val errorGraph = graph("Closed Loop Error", Inch)

    override fun LiftHardware.output(value: OffloadedOutput) {
        value.with(hardware.conversions.safeties).writeTo(esc)

        errorGraph(currentTime, hardware.conversions.native.realPosition(esc.closedLoopError))
    }
}
