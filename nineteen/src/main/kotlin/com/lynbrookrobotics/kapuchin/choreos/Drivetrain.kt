package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.choreographies.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.launch

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

    val autoAlign by driver.autoAlign.readEagerly().withoutStamps

    choreography {
        try {
            launchWhenever(
                    { drivetrain.routine == null } to choreography {
                        drivetrain.teleop(driver)
                    }
            )
            runWhenever(
                    { autoAlign } to choreography {
                        //if (limelight != null && collectorSlider != null && lift != null) {
//limeLineAlign(limelight, collectorSlider, lift)
//}
                        launch { collectorSlider?.trackLine(lineScanner, electrical) }
                        drivetrain.lineActiveTracking(
                                3.FootPerSecond,
                                collectorSlider
                                        ?.run { (min - 0.5.Inch)..(max + 0.5.Inch) }
                                        ?: -5.Inch..5.Inch,
                                lineScanner
                        )
                    }
            )
            freeze()
        } catch (t: Throwable) {
            log(Error, t) { "The drivetrain teleop control is exiting!!!" }
            throw t
        }
    }
}