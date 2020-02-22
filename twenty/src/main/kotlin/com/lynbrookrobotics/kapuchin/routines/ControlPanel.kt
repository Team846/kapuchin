package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.control.electrical.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*

suspend fun ControlPanelPivotComponent.set(state: ControlPanelPivotState) = startRoutine("Set") {
    controller { state }
}

suspend fun ControlPanelSpinnerComponent.spinStage2(electrical: ElectricalSystemHardware) = startRoutine("Stage 2") {
    val color by hardware.color.readOnTick.withStamps
    val encoder by hardware.encoderPosition.readOnTick.withStamps

    val vBat by electrical.batteryVoltage.readEagerly.withoutStamps

    var lastColorIndex = hardware.conversions.indexColor(color.y)
    var lastEncoder = encoder

    val target = 4.5.Turn

    var totalAngle = 0.Turn
    val ddt = differentiator(::p, currentTime, totalAngle)
    controller {
        val colorIndex = hardware.conversions.indexColor(color.y)

        val omega: AngularVelocity

        // TODO: Fix with Kotlin Contracts. Make copy because Kotlin compiler is worried about concurrency.
        val lastColorIndexCopy = lastColorIndex

        if (lastColorIndexCopy == null || colorIndex == null) {
            log(Warning) {
                "Could not read control panel colors\n" +
                        "lastColorIndex == ${lastColorIndex}\n" +
                        "color == ${color.y.red withDecimals 2} R, ${color.y.green withDecimals 2} G, ${color.y.blue withDecimals 2} B\n" +
                        "colorIndex == ${colorIndex}\n"
            }
            totalAngle += hardware.conversions.encoderPositionDelta(encoder.y - lastEncoder.y)
            omega = ddt(encoder.x, totalAngle)
        } else {
            totalAngle += hardware.conversions.colorPositionDelta(lastColorIndexCopy, colorIndex)
            omega = ddt(color.x, totalAngle)
        }
        lastColorIndex = colorIndex

        val error = target - totalAngle
        voltageToDutyCycle(kP * error - kD * omega, vBat)
                .takeIf { error in `±`(0.25.Turn) }
    }
}