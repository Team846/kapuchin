package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {

    val deployCargo by operator.deployCargo.readEagerly().withoutStamps
    val softDeployCargo by operator.softDeployCargo.readEagerly().withoutStamps
    val deployPanel by operator.deployPanel.readEagerly().withoutStamps
    val collectCargo by driver.collectCargo.readEagerly().withoutStamps
    val collectPanel by operator.collectPanel.readEagerly().withoutStamps
//    val collectGroundPanel by operator.collectGroundPanel.readEagerly().withoutStamps
    val operatorLineTracking by operator.lineTracking.readEagerly().withoutStamps
    val driverLineTracking by driver.lineTracking.readEagerly().withoutStamps
    val centerAll by operator.centerAll.readEagerly().withoutStamps
    val pivotDown by operator.pivotDown.readEagerly().withoutStamps
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { deployCargo || softDeployCargo } to choreography { deployCargo(softDeployCargo) },
                { deployPanel } to choreography { deployPanel() },
                { collectCargo } to choreography { collectCargo() },
                { collectPanel } to choreography { collectPanel() },
//                { collectGroundPanel } to choreography { collectGroundPanel() },
                { operatorLineTracking || driverLineTracking } to choreography { trackLine() },
//                { centerSlider } to choreography { centerSlider() },
//                { centerCargo } to choreography { centerCargo() },
                { centerAll } to choreography { centerAll() },
                { pivotDown } to choreography { pivotDown() },
                { !sliderPrecision.isZero } to choreography { collectorSlider?.manualOverride(operator) }
        )
    }
}

suspend fun Subsystems.deployCargo(soft: Boolean) {
    //Center collector slider
    collectorSlider?.set(0.Inch, electrical)

    //Eject cargo
    collectorRollers?.spin(electrical, if (soft) collectorRollers.cargoReleaseSpeed / 2 else collectorRollers.cargoReleaseSpeed)
    freeze()
}

suspend fun Subsystems.pivotDown() {
    collectorPivot?.set(CollectorPivotState.Down)
    freeze()
}

suspend fun Subsystems.deployPanel() = supervisorScope {
    //Eject panel
    val hookSliderOut = launch { hookSlider?.set(HookSliderState.Out) }

    try {
        freeze()
    } finally {
        withContext(NonCancellable) {
            val hookDown = launch { hook?.set(HookPosition.Down) }
            withTimeout(1.Second) {
                lift?.set(lift.hardware.position.optimizedRead(
                        currentTime, 0.Second
                ).y - 3.5.Inch, 0.1.Inch)
            }
            hookSliderOut.cancel()
            delay(0.2.Second)
            hookDown.cancel()

        }
    }

//    //Lift down
//    withTimeout(1.Second) { lift?.set(lift.collectPanel, 1.Inch) }
//    launch { lift?.set(lift.collectPanel, 0.Inch) }
//
//    //Hook down, slider out
//    val hookDown = launch { hook?.set(HookPosition.Down) }
//    delay(0.5.Second)
//    val hookSliderOut = launch { hookSlider?.set(HookSliderState.Out) }
//
//    try {
//        freeze()
//    } finally {
//        withContext(NonCancellable) {
//            lift?.set(lift.hardware.position.optimizedRead(
//                    currentTime, 0.Second
//            ).y + 3.Inch)
//            hookDown.cancel()
//            delay(0.2.Second)
//            hookSliderOut.cancel()
//        }
//    }
}

suspend fun Subsystems.collectCargo() = supervisorScope {
    //Start rollers
    launch { handoffRollers?.spin(handoffRollers.cargoCollectSpeed) }
    launch { collectorRollers?.spin(electrical, collectorRollers.cargoCollectSpeed) }

    //Center slider
    collectorSlider?.set(0.Inch, electrical)

    //Set handoff pivot down
    withTimeout(1.Second) { handoffPivot?.set(handoffPivot.collectPosition, 10.Degree) }
    val handoffPivotSet = launch { handoffPivot?.set(handoffPivot.collectPosition, 0.Degree) }

    //lift, collector down
    launch { lift?.set(lift.collectCargo, 0.Inch) }
    launch { collectorPivot?.set(CollectorPivotState.Down) }

    try {
        freeze()
    } finally {
        withContext(NonCancellable) {
            withTimeout(1.Second) {
                handoffPivotSet.join()
            }
        }
    }
}

suspend fun Subsystems.collectPanel() = supervisorScope {
    //Lift down
    withTimeout(1.Second) { lift?.set(lift.collectPanel, 1.Inch) }
    launch { lift?.set(lift.collectPanel, 0.Inch) }

    //Hook down, slider out
    val hookDown = launch { hook?.set(HookPosition.Down) }
    delay(0.5.Second)
    val hookSliderOut = launch { hookSlider?.set(HookSliderState.Out) }

    try {
        freeze()
    } finally {
        withContext(NonCancellable) {
            hookDown.cancel()
            delay(0.3.Second)
            withTimeout(1.2.Second) {
                lift?.set(lift.hardware.position.optimizedRead(
                        currentTime, 0.Second
                ).y + 7.Inch, 0.1.Inch)
            }
            hookSliderOut.cancel()
        }
    }
}

suspend fun Subsystems.trackLine() = coroutineScope {
    //Track line with slider
    collectorSlider?.trackLine(0.5.Inch, lineScanner, electrical)
    freeze()
}

suspend fun Subsystems.centerAll() = coroutineScope {
    launch { centerSlider() }
//    launch { centerCargo() }
    freeze()
}

suspend fun Subsystems.centerSlider() {
    collectorSlider?.set(
            0.Inch,
            electrical
    )
    freeze()
}

suspend fun Subsystems.centerCargo() {
    collectorRollers?.spin(
            electrical,
            bottom = collectorRollers.cargoCenterSpeed, //+ collectorRollers.inBias, // top out
            top = -collectorRollers.cargoCenterSpeed // top in
    )
    freeze()
}

/**
 * Collect panel from the ground
 *
 * Ends with:
 * CollectorPivot - Up
 * CollectorSlider - Center
 * HandoffPivot - Handoff position
 * VelcroPivot - Down
 * Hook - Up
 * Lift - Collect ground panel height
 */
suspend fun Subsystems.collectGroundPanel() = coroutineScope {
    //Center slider
    collectorSlider?.set(0.Inch, electrical)

    //Lift down
    launch { lift?.set(lift.collectGroundPanel) }

    //Handoff, velcro, hook down
    launch { handoffPivot?.set(handoffPivot.collectPosition) }
    launch { velcroPivot?.set(VelcroPivotPosition.Down) }
    launch { hook?.set(HookPosition.Down) }

    freeze()
}

