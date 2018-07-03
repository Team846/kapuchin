package com.lynbrookrobotics.kapuchin.routines.teleop

import com.lynbrookrobotics.kapuchin.control.withToleranceOf
import com.lynbrookrobotics.kapuchin.logging.Level.Warning
import com.lynbrookrobotics.kapuchin.logging.log
import com.lynbrookrobotics.kapuchin.routines.autoroutine
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.LiftComponent
import com.lynbrookrobotics.kapuchin.subsystems.climber.HooksComponent
import com.lynbrookrobotics.kapuchin.subsystems.climber.WinchComponent
import info.kunalsheth.units.generated.Time

suspend fun HooksComponent.teleop(driver: DriverHardware, lift: LiftComponent, isFinished: HooksComponent.(Time) -> Boolean) {
    var state = false
    val isTriggered by driver.deployHooks.readEagerly.withoutStamps
    val liftPosition by lift.hardware.position.readEagerly.withoutStamps

    autoroutine(
            newController = {
                if (isTriggered) state = !state
                if (state && liftPosition !in lift.collectHeight withToleranceOf lift.positionTolerance) {
                    log(Warning) { "Cannot deploy hooks until lift is lowered" }
                    false
                } else state
            },
            isFinished = isFinished
    )
}

suspend fun WinchComponent.teleop(driver: DriverHardware, isFinished: WinchComponent.(Time) -> Boolean) {

//    autoroutine(
//            newController = {
//                if ()
//            }
//    )
}