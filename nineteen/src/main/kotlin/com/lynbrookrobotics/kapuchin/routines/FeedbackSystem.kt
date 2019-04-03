package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.FeedbackSystemComponent.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import info.kunalsheth.units.generated.*
import java.awt.Color

suspend fun FeedbackSystemComponent.vibrateOnAlign(lineScanner: LineScannerHardware, slider: CollectorSliderComponent) = startRoutine("Vibrate on align") {
    val target by lineScanner.linePosition.readEagerly.withoutStamps
    val current by slider.hardware.position.readEagerly.withoutStamps

    controller {
        target?.let { snapshot ->
            val error = snapshot - current

            if (error.abs < 1.5.Inch) Feedback(
                    wheelRumble = 100.Percent,
                    stickRumble = 100.Percent,
                    xboxLeftRumble = 100.Percent, xboxRightRumble = 100.Percent,
                    ledColor = Color.RED
            ) else null
        } ?: Feedback()
    }
}