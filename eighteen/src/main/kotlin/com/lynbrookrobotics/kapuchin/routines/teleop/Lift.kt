package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.control.`±`
import com.lynbrookrobotics.kapuchin.hardware.Sensor
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PercentOutput
import com.lynbrookrobotics.kapuchin.hardware.offloaded.PositionOutput
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.LiftComponent
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.`±`
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

suspend fun LiftComponent.teleop(driver: DriverHardware) = startRoutine("teleop") {
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

    controller {
        val adjust = twistAdjustRange * twistAdjust

        if (overrideLift) PercentOutput(manualOverride)
        else PositionOutput(hardware.offloadedSettings.native(positionGains),
                hardware.offloadedSettings.native(when {
                    toCollect || toDeployHooks -> collectHeight
                    toExchange -> exchangeHeight + adjust
                    toSwitch -> switchHeight + adjust
                    toLowScale -> lowScaleHeight + adjust
                    toHighScale -> highScaleHeight + adjust
                    toMaxHeight -> hardware.maxHeight + adjust
                    else -> currentPosition
                }
                ))
    }
}

suspend fun LiftComponent.singleStackTeleop(driver: DriverHardware) = startRoutine("single stack teleop") {
    fun <I> r(s: Sensor<I>) = s.readWithEventLoop.withoutStamps

    val twistAdjust by r(driver.twistAdjust)
    val toCollect by r(driver.collect)
    val toLowScale by r(driver.lowScale)
    val toMaxHeight by r(driver.maxHeight)

    val upStack by r(driver.upCubeStack)
    val downStack by r(driver.downCubeStack)
    val zeroStack by r(driver.zeroCubeStack)

    val manualOverride by r(driver.manualOverride)
    val overrideLift by r(driver.manualLift)

    val toDeployHooks by r(driver.deployHooks)

    val currentPosition by hardware.position.readOnTick.withoutStamps

    var cubeZero = 23.35.Inch
    var cubeIndex = -1
    var stackingMode = false

    controller {
        val adjust = twistAdjustRange * twistAdjust
        fun stackTarget() = cubeHeight * cubeIndex + cubeZero + adjust

        if (zeroStack) {
            cubeZero = currentPosition
            cubeIndex = 0
        }

        val target = when {
            toCollect || toDeployHooks -> {
                stackingMode = false
                collectHeight
            }
            upStack -> {
                stackingMode = true
                cubeIndex++
                stackTarget()
            }
            downStack -> {
                stackingMode = true
                cubeIndex--
                stackTarget()
            }
            toLowScale -> {
                stackingMode = false
                lowScaleHeight + adjust
            }
            toMaxHeight -> {
                stackingMode = false
                hardware.maxHeight + adjust
            }
            else -> if(stackingMode) stackTarget() else currentPosition
        }

        if (overrideLift) PercentOutput(manualOverride)
        else PositionOutput(
                hardware.offloadedSettings.native(positionGains),
                hardware.offloadedSettings.native(target)
        )
    }
}

suspend fun LiftComponent.cubeStackTeleop(driver: DriverHardware) = startRoutine("cube stacking teleop") {
    fun <I> r(s: Sensor<I>) = s.readWithEventLoop.withoutStamps

    val twistAdjust by r(driver.twistAdjust)
    val toCollect by r(driver.collect)
    val toLowScale by r(driver.lowScale)
    val toMaxHeight by r(driver.maxHeight)

    val upStack by r(driver.upCubeStack)
    val downStack by r(driver.downCubeStack)
    val zeroStack by r(driver.zeroCubeStack)

    val manualOverride by r(driver.manualOverride)
    val overrideLift by r(driver.manualLift)

    val toDeployHooks by r(driver.deployHooks)

    val currentPosition by hardware.position.readOnTick.withoutStamps

    var persistentTarget: Length? = null
    var zeroHeight = collectHeight

    controller {
        val downCubeHeight: Length
        val upCubeHeight: Length

        if (zeroStack) zeroHeight = currentPosition
        val cubeRelativePosition = currentPosition - zeroHeight

        val currentCubeHeight = (cubeRelativePosition / cubeHeight).Each
        val stackingMode = currentCubeHeight in round(currentCubeHeight) `±` (positionTolerance / cubeHeight).Each
        if (stackingMode) {
            val currentCubeIndex = round(currentCubeHeight)
            downCubeHeight = cubeHeight * (currentCubeIndex - 1) + zeroHeight
            upCubeHeight = cubeHeight * (currentCubeIndex + 1) + zeroHeight
        } else {
            downCubeHeight = cubeHeight * floor(currentCubeHeight) + zeroHeight
            upCubeHeight = cubeHeight * ceil(currentCubeHeight) + zeroHeight
        }

        val adjust = twistAdjustRange * twistAdjust

        val target = when {
            toCollect || toDeployHooks -> collectHeight.also { persistentTarget = null }
            upStack -> upCubeHeight.also { persistentTarget = it }
            downStack -> downCubeHeight.also { persistentTarget = it }
            toLowScale -> (lowScaleHeight + adjust).also { persistentTarget = null }
            toMaxHeight -> (hardware.maxHeight + adjust).also { persistentTarget = null }
            else -> persistentTarget ?: currentPosition
        }

        if (overrideLift) PercentOutput(manualOverride).also { persistentTarget = null }
        else PositionOutput(
                hardware.offloadedSettings.native(positionGains),
                hardware.offloadedSettings.native(target)
        )
    }
}

suspend fun LiftComponent.to(height: Length, tolerance: Length = positionTolerance) = startRoutine("to") {
    val position by hardware.position.readOnTick.withoutStamps
    controller {
        if (position in height `±` tolerance) null
        else PositionOutput(
                hardware.offloadedSettings.native(positionGains),
                hardware.offloadedSettings.native(height)
        )
    }
}