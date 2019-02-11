package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.hardware.tickstoserial.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

suspend fun DrivetrainComponent.warmup() = startRoutine("warmup") {

    fun r() = Math.random()
    val conv = DrivetrainConversions(hardware)

    controller {
        val startTime = currentTime
        while (currentTime - startTime < hardware.period * 90.Percent) {
            val (l, r) = TicksToSerialValue((r() * 0xFF).toInt())
            conv.accumulateOdometry(l, r)
        }
        val (x, y, _) = Position(conv.matrixTracking.x, conv.matrixTracking.y, conv.matrixTracking.bearing)


        val targetA = 1.Turn * r()
        val errorA = targetA `coterminal -` 1.Turn * r()
        val pA = bearingKp * errorA

        val targetL = maxSpeed * r() + pA + x / Second
        val targetR = maxSpeed * r() - pA + y / Second

        val nativeL = hardware.conversions.nativeConversion.native(targetL)
        val nativeR = hardware.conversions.nativeConversion.native(targetR)

        TwoSided(
                VelocityOutput(velocityGains, nativeL),
                VelocityOutput(velocityGains, nativeR)
        )
    }
}