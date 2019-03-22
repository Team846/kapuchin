package com.lynbrookrobotics.kapuchin.routines

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.hardware.offloaded.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*

suspend fun LiftComponent.liftTeleop(driver: DriverHardware, operator: OperatorHardware) = startRoutine("Lift teleop") {

    val currentPosition by hardware.position.readOnTick.withoutStamps

    val liftDown by driver.liftDown.readEagerly.withoutStamps

    val lowPanelHeight by operator.lowPanelHeight.readEagerly.withoutStamps
    val lowCargoHeight by operator.lowCargoHeight.readEagerly.withoutStamps

    val midPanelHeight by operator.midPanelHeight.readEagerly.withoutStamps
    val midCargoHeight by operator.midCargoHeight.readEagerly.withoutStamps
    val cargoShipCargoHeight by operator.cargoShipCargoHeight.readEagerly.withoutStamps

    val highPanelHeight by operator.highPanelHeight.readEagerly.withoutStamps
    val highCargoHeight by operator.highCargoHeight.readEagerly.withoutStamps

    val liftPrecision by operator.liftPrecision.readEagerly.withoutStamps

    var lastTarget = currentPosition
    controller {
        if(liftPrecision.isZero) {
            val target = when {
                liftDown -> collectPanel
                lowPanelHeight -> panelLowRocket
                lowCargoHeight -> cargoLowRocket
                midPanelHeight -> panelMidRocket
                midCargoHeight -> cargoMidRocket
                cargoShipCargoHeight -> cargoCargoShip
                highPanelHeight -> panelHighRocket
                highCargoHeight -> cargoHighRocket
                else -> lastTarget
            }

            with(hardware.conversions.native) {
                PositionOutput(
                        OffloadedPidGains(
                                kP = native(kP),
                                kI = 0.0,
                                kD = native(kD)
                        ), native(target)
                )
            }.also {
                lastTarget = target
            }
        } else {
            lastTarget = currentPosition
            PercentOutput(liftPrecision)
        }
    }
}

suspend fun LiftComponent.set(target: Length, tolerance: Length = 2.Inch) = startRoutine("Set") {

    val current by hardware.position.readOnTick.withoutStamps

    controller {
        with(hardware.conversions.native) {
            PositionOutput(
                    OffloadedPidGains(
                            kP = native(kP),
                            kI = 0.0,
                            kD = native(kD)
                    ), native(target)
            ).takeUnless {
                (target - current).abs < tolerance
            }
        }
    }
}

suspend fun LiftComponent.manualOverride(operator: OperatorHardware) = startRoutine("Manual override") {

    val liftPrecision by operator.liftPrecision.readEagerly.withoutStamps
    val position by hardware.position.readEagerly.withoutStamps

    var targetting = position.also {}
    controller {
        if (liftPrecision.isZero) with(hardware.conversions.native) {
            PositionOutput(
                    OffloadedPidGains(
                            kP = native(kP),
                            kI = 0.0,
                            kD = native(kD)
                    ), native(targetting)
            )
        }
        else {
            targetting = position + 5.Inch * liftPrecision.signum
            PercentOutput(liftPrecision)
        }
    }
}