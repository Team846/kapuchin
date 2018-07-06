package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.hardware.Sensor
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PositionOutput
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.runRoutine
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.LiftComponent
import info.kunalsheth.units.generated.Length
import info.kunalsheth.units.generated.Tick
import info.kunalsheth.units.generated.Time

suspend fun LiftComponent.teleop(driver: DriverHardware, isFinished: LiftComponent.(Time) -> Boolean) {
    fun <I> r(s: Sensor<I>) = s.readWithEventLoop.withoutStamps

    val twistAdjust by r(driver.twistAdjust)
    val toCollect by r(driver.collect)
    val toExchange by r(driver.exchange)
    val toSwitch by r(driver.switch)
    val toLowScale by r(driver.lowScale)
    val toHighScale by r(driver.highScale)
    val toMaxHeight by r(driver.maxHeight)

    val manualOverride by r(driver.manualOverride)
    val overrideLift by r(driver.manualLift)

    val toDeployHooks by r(driver.deployHooks)

    val currentPosition by hardware.position.readOnTick.withoutStamps

    runRoutine("Teleop",
            newController = {
                if (overrideLift) PercentOutput(manualOverride.Tick)
                else PositionOutput(hardware.offloadedSettings.native(positionGains),
                        hardware.offloadedSettings.native(when {
                            toCollect || toDeployHooks -> collectHeight
                            toExchange -> exchangeHeight
                            toSwitch -> switchHeight
                            toLowScale -> lowScaleHeight
                            toHighScale -> highScaleHeight
                            toMaxHeight -> hardware.maxHeight
                            else -> currentPosition
                        } + twistAdjustRange * twistAdjust
                        ))
            },
            isFinished = isFinished
    )
}

suspend fun LiftComponent.to(height: Length, tolerance: Length = positionTolerance) {
    val position by hardware.position.readOnTick.withoutStamps
    runRoutine("To Height",
            newController = {
                PositionOutput(
                        hardware.offloadedSettings.native(positionGains),
                        hardware.offloadedSettings.native(height)
                )
            },
            isFinished = {
                position in height withToleranceOf tolerance
            }
    )
}