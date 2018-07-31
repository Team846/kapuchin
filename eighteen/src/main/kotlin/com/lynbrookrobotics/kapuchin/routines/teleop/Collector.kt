package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.collector.ClampComponent
import com.lynbrookrobotics.kapuchin.subsystems.collector.PivotComponent
import com.lynbrookrobotics.kapuchin.subsystems.collector.RollersComponent
import info.kunalsheth.units.generated.*

suspend fun RollersComponent.teleop(driver: DriverHardware) {
    val toCollect by driver.collect.readEagerly.withoutStamps
    val toPurge by driver.purge.readEagerly.withoutStamps

    runRoutine("Teleop") {
        val adjust = cubeAdjustStrength * if ((it * cubeAdjustCycle).Each.toInt() % 2 == 0) 1 else -1
        when {
            toCollect -> TwoSided(-collectStrength + adjust, -collectStrength - adjust)
            toPurge -> TwoSided(purgeStrength)
            else -> TwoSided(-cubeHoldStrength)
        }
    }
}

suspend fun RollersComponent.purge() = runRoutine("Purge") { TwoSided(purgeStrength) }
suspend fun RollersComponent.collect() = runRoutine("Collect") { TwoSided(-collectStrength) }

suspend fun PivotComponent.teleop(driver: DriverHardware) {
    val isTriggered by driver.pivotDown.readEagerly.withoutStamps
    runRoutine("Teleop") { isTriggered }
}

suspend fun PivotComponent.pivot() = runRoutine("Pivot Down") { true }

suspend fun ClampComponent.teleop(driver: DriverHardware) {
    val isTriggered by driver.openClamp.readEagerly.withoutStamps
    runRoutine("Teleop") { isTriggered }
}

suspend fun ClampComponent.open() = runRoutine("Open Clamp") { true }