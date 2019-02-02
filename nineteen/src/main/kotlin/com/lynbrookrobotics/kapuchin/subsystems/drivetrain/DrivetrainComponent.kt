package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.control.data.Gain
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.OffloadedPidGains
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.Component
import com.lynbrookrobotics.kapuchin.timing.clock.Ticker
import com.lynbrookrobotics.kapuchin.timing.currentTime
import com.lynbrookrobotics.kapuchin.timing.monitoring.RealtimeChecker.Companion.realtimeChecker
import info.kunalsheth.units.generated.*

class DrivetrainComponent(hardware: DrivetrainHardware) : Component<DrivetrainComponent, DrivetrainHardware, TwoSided<OffloadedOutput>>(hardware) {

    val maxLeftSpeed by pref(13, FootPerSecond)
    val maxRightSpeed by pref(13.3, FootPerSecond)
    val maxSpeed get() = maxLeftSpeed min maxRightSpeed

    val velocityGains by pref {
        val kP by pref(10, Volt, 2, FootPerSecond)
        ({ OffloadedPidGains(
                hardware.conversions.nativeConversion.native(kP),
                0.0, 0.0,
                hardware.conversions.nativeConversion.native(
                        Gain(hardware.operatingVoltage, maxSpeed)
                )
        ) })
    }

    val bearingKp by pref(2, FootPerSecond, 60, Degree)
    val bearingKd by pref(0, FootPerSecond, 60, DegreePerSecond)

    val lineScannerLead by pref(2.5, Foot)
  
    override val fallbackController: DrivetrainComponent.(Time) -> TwoSided<OffloadedOutput> = {
        TwoSided(PercentOutput(0.Percent))
    }

    private val leftOutputGraph = graph("Left Output", Volt)
    private val rightOutputGraph = graph("Right Output", Volt)
    override fun DrivetrainHardware.output(value: TwoSided<OffloadedOutput>) {
        leftLazyOutput(value.left)
        rightLazyOutput(value.right)

        leftOutputGraph(currentTime, hardware.leftMasterEsc.motorOutputVoltage.Volt)
        rightOutputGraph(currentTime, hardware.rightMasterEsc.motorOutputVoltage.Volt)
    }

    init {
        if (clock is Ticker) clock.realtimeChecker(hardware.jitterPulsePin::set, { hardware.jitterReadPin.period.Second })
    }
}