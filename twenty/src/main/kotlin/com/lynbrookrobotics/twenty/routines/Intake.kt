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
            val initialSpeed = (leftSpeed + rightSpeed)
            var scaledVoltage = hardware.escConfig.voltageCompSaturation * scale * (initialSpeed / drivetrain.maxSpeed) // set it to 2 * drivetrain speed
            if(initialSpeed > hardware.threshold){
                scaledVoltage = hardware.escConfig.voltageCompSaturation * scale * (hardware.threshold / drivetrain.maxSpeed) // if < threshold, default to threshold
            }
            log(Debug){"$scaledVoltage"}
            PercentOutput(hardware.escConfig, voltageToDutyCycle(-scaledVoltage, vBat))

        }
    }
