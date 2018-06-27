package com.lynbrookrobotics.kapuchin.subsystems.drivetrain.routines

import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.hardware.offloaded.VelocityOutput
import com.lynbrookrobotics.kapuchin.routines.autoroutine
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import info.kunalsheth.units.generated.Time

suspend fun DrivetrainComponent.teleopDrive(isFinished: DrivetrainComponent.(Time) -> Boolean) {

    val driverStick by driver.driverStick
    val driverWheel by driver.driverWheel

    val leftGains = offloadedSettings.native(leftVelocityGains)
    val rightGains = offloadedSettings.native(rightVelocityGains)

    autoroutine(
            newController = {
                val forward = topSpeed * driverStick.y
                val turn = topSpeed * driverWheel.x
                TwoSided(
                        VelocityOutput(leftGains, offloadedSettings.native(forward + turn)),
                        VelocityOutput(rightGains, offloadedSettings.native(forward - turn))
                )
            },
            isFinished = isFinished
    )
}