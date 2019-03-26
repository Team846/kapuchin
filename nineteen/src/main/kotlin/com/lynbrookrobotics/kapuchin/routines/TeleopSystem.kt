package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import info.kunalsheth.units.generated.*
import java.awt.Color

suspend fun TeleopComponent.vibrateOnAlign(lineScanner: LineScannerHardware, slider: CollectorSliderComponent) = startRoutine("Vibrate on align") {
    val target by lineScanner.linePosition.readOnTick.withoutStamps
    val current by slider.hardware.position.readOnTick.withoutStamps

    controller {
        target?.let { snapshot ->
            val error = snapshot - current
            val balance = (snapshot - slider.min) / (slider.max - slider.min)
            val rumble = TwoSided(
                    100.Percent * (1.Each - balance),
                    100.Percent * balance
            )

            if (error.abs < 1.Inch) {
                rumble to Color.YELLOW
            } else null
        } ?: fallbackRumble to fallbackColor
    }
}