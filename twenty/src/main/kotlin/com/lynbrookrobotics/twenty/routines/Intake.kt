package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.twenty.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.intake.*
import info.kunalsheth.units.generated.*

suspend fun IntakeSliderComponent.set(target: IntakeSliderState) = startRoutine("Set") {
    controller { target }
}

suspend fun IntakeRollersComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}

suspend fun IntakeRollersComponent.optimalEat(drivetrain: DrivetrainComponent, electrical: ElectricalSystemHardware) =
    startRoutine("Optimal Eat") {

        val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

        val leftSpeed by drivetrain.hardware.leftSpeed.readEagerly.withoutStamps
        val rightSpeed by drivetrain.hardware.rightSpeed.readEagerly.withoutStamps

        controller {
//            val voltage =
//                eatSpeed - hardware.escConfig.voltageCompSaturation * ((leftSpeed + rightSpeed) / (drivetrain.maxSpeed * 2))
            val rpm = (leftSpeed / drivetrain.hardware.conversions.wheelRadius.left + rightSpeed / drivetrain.hardware.conversions.wheelRadius.right) / 2.0
            var voltage = hardware.escConfig.voltageCompSaturation * scale * rpm / (motorRPM / 1.Minute)
            if (voltage < defaultVoltage) voltage = defaultVoltage
            log(Debug) {"$voltage"}
            PercentOutput(hardware.escConfig, voltageToDutyCycle(voltage, vBat))

        }
    }
