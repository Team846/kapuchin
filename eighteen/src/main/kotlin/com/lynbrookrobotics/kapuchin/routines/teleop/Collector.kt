package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.control.math.TwoSided
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.runRoutine
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.collector.RollersComponent
import info.kunalsheth.units.generated.Tick
import info.kunalsheth.units.generated.Time

suspend fun RollersComponent.teleop(driver: DriverHardware, isFinished: RollersComponent.(Time) -> Boolean) {
    val toCollect by driver.collect.readEagerly.withoutStamps
    val toPurge by driver.purge.readEagerly.withoutStamps

    runRoutine("Teleop",
            newController = {
                val adjust = cubeAdjustStrength * if ((it * cubeAdjustCycle).Tick.toInt() % 2 == 0) 1 else -1
                when {
                    toCollect -> TwoSided(-collectStrength + adjust, -collectStrength - adjust)
                    toPurge -> TwoSided(purgeStrength)
                    else -> TwoSided(-cubeHoldStrength)
                }
            },
            isFinished = isFinished
    )
}

suspend fun RollersComponent.purge(isFinished: RollersComponent.(Time) -> Boolean) = runRoutine("Purge",
        newController = { TwoSided(purgeStrength) },
        isFinished = isFinished
)

suspend fun RollersComponent.collect(isFinished: RollersComponent.(Time) -> Boolean) = runRoutine("Collect",
        newController = { TwoSided(-collectStrength) },
        isFinished = isFinished
)

