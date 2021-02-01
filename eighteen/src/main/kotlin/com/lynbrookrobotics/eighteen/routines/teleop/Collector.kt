package com.lynbrookrobotics.eighteen.routines.teleop

import com.lynbrookrobotics.eighteen.subsystems.DriverHardware
import com.lynbrookrobotics.eighteen.subsystems.collector.ClampComponent
import com.lynbrookrobotics.eighteen.subsystems.collector.PivotComponent
import com.lynbrookrobotics.eighteen.subsystems.collector.RollersComponent
import com.lynbrookrobotics.kapuchin.control.data.*
import info.kunalsheth.units.generated.*

suspend fun RollersComponent.teleop(driver: DriverHardware) = startRoutine("teleop") {
    val toCollect by driver.collect.readEagerly.withoutStamps
    val toPurge by driver.purge.readEagerly.withoutStamps

    controller {
        when {
            toCollect -> cornerAdjustingCollection(it)
            toPurge -> TwoSided(purgeStrength)
            else -> TwoSided(-cubeHoldStrength)
        }
    }
}

suspend fun RollersComponent.purge() = startRoutine("purge") { controller { TwoSided(purgeStrength) } }
suspend fun RollersComponent.collect() = startRoutine("collect") { controller { cornerAdjustingCollection(it) } }

private fun RollersComponent.cornerAdjustingCollection(now: Time): TwoSided<DutyCycle> {
    val adjust = cubeAdjustStrength * if ((now * cubeAdjustCycle).Each.toInt() % 2 == 0) 1 else -1
    return TwoSided(-collectStrength + adjust, -collectStrength - adjust)
}

suspend fun PivotComponent.teleop(driver: DriverHardware) = startRoutine("teleop") {
    val isTriggered by driver.pivotDown.readEagerly.withoutStamps
    controller { isTriggered }
}

suspend fun PivotComponent.pivot() = startRoutine("down") { controller { true } }

suspend fun ClampComponent.teleop(driver: DriverHardware) = startRoutine("teleop") {
    val isTriggered by driver.openClamp.readEagerly.withoutStamps
    controller { isTriggered }
}

suspend fun ClampComponent.open() = startRoutine("open") { controller { true } }