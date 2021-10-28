package com.lynbrookrobotics.twenty.subsystems.shooter.flywheel

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.Subsystems
import info.kunalsheth.units.generated.*

class FlywheelComponent(hardware: FlywheelHardware) :
    Component<FlywheelComponent, FlywheelHardware, OffloadedOutput>(hardware, Subsystems.shooterTicker) {

    val presetAnitez by pref(3000, Rpm)
    val presetClose by pref(4500, Rpm)
    val presetMed by pref(5000, Rpm)
    val presetFar by pref(6000, Rpm)
    private val fudgeFactor by pref(2, Percent)

    var rpmPercentage = 100.Percent
        private set
    private val maxSpeed by pref(9632, Rpm)

    val tolerance by pref(10, Rpm)

    val velocityGains by pref {
        val kP by pref(10, Volt, 100, Rpm)
        val kF by pref(110, Percent)
        ({
            OffloadedEscGains(
                kP = hardware.conversions.encoder.native(kP),
                kF = hardware.conversions.encoder.native(
                    Gain(hardware.escConfig.voltageCompSaturation, maxSpeed)
                ) * kF.Each
            )
        })
    }

    fun changeFlywheelSpeed(increment: Boolean) {
        val value = if (increment) fudgeFactor else -fudgeFactor
        rpmPercentage += value
        if (rpmPercentage < 0.Percent) rpmPercentage = 0.Percent
    }

    private val idleOutput by pref(50, Percent)

    private val rpmPercentageOfGraph = graph("rpmPercentageOf", Each)
    val innerPortDistanceThreshold by pref(20, Foot)
    val innerOuterDelta by pref(29.25, Inch)

    override val fallbackController: FlywheelComponent.(Time) -> OffloadedOutput = {
        PercentOutput(hardware.escConfig, idleOutput)
    }

    override fun FlywheelHardware.output(value: OffloadedOutput) {
        value.writeTo(masterEsc, pidController)
        rpmPercentageOfGraph(currentTime, rpmPercentage)
    }

    init {
        Subsystems.uiTicker.runOnTick { time ->
            rpmPercentageOfGraph(time, rpmPercentage)
        }
    }
}
