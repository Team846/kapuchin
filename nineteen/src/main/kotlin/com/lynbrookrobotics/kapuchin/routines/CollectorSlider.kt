package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*

suspend fun CollectorSliderComponent.trackLine(tolerance: Length, lineScanner: LineScannerHardware, electrical: ElectricalSystemHardware) = startRoutine("Track line") {

    val target by lineScanner.linePosition.readOnTick.withoutStamps
    val current by hardware.position.readOnTick.withoutStamps
    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    controller {
        target?.let { t ->
            val error = t - current
            val voltage = kP * error

            voltageToDutyCycle(voltage, vBat).takeIf {
                current in t `±` tolerance
            }
        }
    }
}
