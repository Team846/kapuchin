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

suspend fun SwerveComponent.teleop(driver: DriverHardware) = startRoutine("Teleop") {
    val accelerator by driver.accelerator.readOnTick.withoutStamps
    val steering by driver.steering.readOnTick.withoutStamps
    val absSteering by driver.absSteering.readOnTick.withoutStamps

    val position by hardware.position.readOnTick.withStamps


    // x1 = strafe, y1 = speed, x2 = rotation
    // TODO: Replace L, W, and r
    // TODO: get x1, y1, and x2
    // TODO: Supply angles + velocities to wheel states
    // TODO: Replace math funcs
    
    val a = x1 - x2 *( L/r)
    val b = x1 + x2 * (L/r)
    val c = y1 - x2 * (W/r)
    val d = y1 + x2 * (W/r)

    val fLSpeed = kotlin.math.sqrt(b * b + c * c)
    val fRSpeed = kotlin.math.sqrt(b*b + d*d)
    val bLSpeed = kotlin.math.sqrt(a*a + d*d)
    val bRSpeed = kotlin.math.sqrt(a*a + c*c)

    val fLAngle = Math.atan2(b, c) * 180 / Math.PI
    val fRAngle = Math.atan2(b, d) * 180 / Math.PI
    val bLAngle = Math.atan2(a, c) * 180 / Math.PI
    val bRAngle = Math.atan2(a, d) * 180 / Math.PI

    // Normalize Speed

    var max = fLSpeed

    max = Math.max(max, fRSpeed)
    max = Math.max(max, bLSpeed)
    max = Math.max(max, fRSpeed)

    if (max > 1) {
        fLSpeed /= max
        fRSpeed /= max
        bRSpeed /= max
        bLSpeed /= max
    }


    // Set Angle of Each wheel here

    // Set Speed for each wheel here```



    controller {

    }
}