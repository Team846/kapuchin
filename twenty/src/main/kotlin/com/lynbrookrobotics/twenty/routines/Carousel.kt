package com.lynbrookrobotics.twenty.routines

import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselComponent
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselSlot
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

suspend fun CarouselComponent.rezero(flipDirection: Boolean = false) = startRoutine("Re-Zero") {
    hardware.isZeroed = false
    val speed = if(flipDirection) -zeroSpeed else zeroSpeed
    controller {
        PercentOutput(hardware.escConfig, speed).takeUnless { hardware.isZeroed }
    }
}

suspend fun CarouselComponent.set(targetPosition: Angle, tolerance: Angle = 2.5.Degree) = startRoutine("Set") {
    val current by hardware.position.readOnTick.withoutStamps

    controller {
        PositionOutput(
            hardware.escConfig, positionGains, hardware.conversions.encoder.native(targetPosition)
        ).takeUnless { targetPosition - current in `±`(tolerance) }
    }
}

suspend fun CarouselComponent.set(target: DutyCycle) = startRoutine("Set") {
    controller { PercentOutput(hardware.escConfig, target) }
}

suspend fun CarouselComponent.whereAreMyBalls() = startChoreo("Re-Index") {
    val color by hardware.color.readEagerly().withoutStamps
    val proximity by hardware.proximity.readEagerly().withoutStamps

    choreography {
        rezero()
        var slotsSkipped = 0
        state.clear()
        for (i in 0 until state.maxBalls) {
            set(i.CarouselSlot)
            val j = launch { set(i.CarouselSlot, 0.Degree) }
            delay(0.1.Second)
            if (hardware.conversions.detectingBall(proximity, color)) {
                state.push(slotsSkipped + 1)
                slotsSkipped = 0
            } else {
                slotsSkipped++
            }
            j.cancel()
        }
    }
}