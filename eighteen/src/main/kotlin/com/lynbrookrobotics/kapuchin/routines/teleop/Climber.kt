package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.routines.autoroutine
import com.lynbrookrobotics.kapuchin.subsystems.ClimberHooksComponent
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import info.kunalsheth.units.generated.Time

suspend fun ClimberHooksComponent.teleop(driver: DriverHardware, isFinished: ClimberHooksComponent.(Time) -> Boolean) {
    var state = false
    val isTriggered by driver.deployHooks.readEagerly.withoutStamps

    return autoroutine(
            newController = {
                if (isTriggered) state = !state
                state
            },
            isFinished = isFinished
    )
}