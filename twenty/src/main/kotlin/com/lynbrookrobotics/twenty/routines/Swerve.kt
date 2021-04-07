package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.twenty.subsystems.*
import com.lynbrookrobotics.twenty.subsystems.driver.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.swerve.SwerveComponent
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlin.math.sqrt

suspend fun SwerveComponent.teleop(driver: DriverHardware) = startRoutine("Teleop") {
    val accelerator by driver.accelerator.readOnTick.withoutStamps
    val steering by driver.steering.readOnTick.withoutStamps
    val absSteering by driver.absSteering.readOnTick.withoutStamps

    val position by hardware.position.readOnTick.withStamps


    controller {
        /*
        strafe = joystick left/right, forward = joystick forward, rotation = steering
        TODO: Supply angles + velocities to wheel states

        Counter-Clockwise Order

        Wheel 1 is frontRight
        Wheel 2 is frontLeft
        Wheel 3 is backLeft
        Wheel 4 is backRight
        */

        val wheel1 = strafe - rotate * (hardware.conversions.trackLength / hardware.conversions.radius)
        val wheel2 = strafe + rotate * (hardware.conversions.trackLength / hardware.conversions.radius)
        val wheel3 = forward - rotate * (hardware.conversions.trackWidth / hardware.conversions.radius)
        val wheel4 = forward + rotate * (hardware.conversions.trackWidth / hardware.conversions.radius)

        var frontLeftSpeed = sqrt(wheel2.pow(2) + wheel3.pow(2))
        var frontRightSpeed = sqrt(wheel2.pow(2) + wheel4.pow(2))
        var backLeftSpeed = sqrt(wheel1.pow(2) + wheel4.pow(2))
        var backRightSpeed = sqrt(wheel1.pow(2) + wheel3.pow(2))

        val frontLeftAngle = atan2(wheel2, wheel3) * 180 / Math.PI
        val frontRightAngle = atan2(wheel2, wheel4) * 180 / Math.PI
        val backLeftAngle = atan2(wheel1, wheel3) * 180 / Math.PI
        val backRightAngle = atan2(wheel1, wheel4) * 180 / Math.PI


        // Normalize Speeds

        val max = maxOf(frontRightSpeed, frontLeftSpeed, backLeftSpeed, backRightSpeed)
        val min = minOf(frontRightSpeed, frontLeftSpeed, backLeftSpeed, backRightSpeed)

        if (max > 1) {
            frontRightSpeed /= max
            frontLeftSpeed /= max
            backRightSpeed /= max
            backLeftSpeed /= max
        }

        if (min < -1) {
            frontRightSpeed /= min * (-1)
            frontLeftSpeed /= min * (-1)
            backRightSpeed /= min * (-1)
            backLeftSpeed /= min * (-1)
        }

        // Rotate each module here

        // Set Speed for each wheel here
    }
}