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
//    val collectPanel by operator.collectPanel.readEagerly().withoutStamps
    val lilDicky by operator.lilDicky.readEagerly().withoutStamps
//    val collectGroundPanel by operator.collectGroundPanel.readEagerly().withoutStamps
    val operatorLineTracking by operator.lineTracking.readEagerly().withoutStamps
//    val driverLineTracking by driver.lineTracking.readEagerly().withoutStamps
    val centerAll by operator.centerAll.readEagerly().withoutStamps
    val pivotDown by operator.pivotDown.readEagerly().withoutStamps
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { deployCargo || softDeployCargo } to choreography { deployCargo(softDeployCargo) },
                { deployPanel } to choreography { deployPanel() },
                { collectCargo } to choreography { collectCargo() },
//                { collectPanel } to choreography { collectPanel() },
                { operatorLineTracking } to choreography { trackLine() },
                { lilDicky } to choreography { lilDicky() },
                { centerAll } to choreography { centerAll() },
                { pivotDown } to choreography { pivotDown() },
                { !sliderPrecision.isZero } to choreography { collectorSlider?.manualOverride(operator) }
        )
    }
}

suspend fun Subsystems.deployCargo(soft: Boolean) {
    //Eject cargo
    try {
        collectorRollers?.spin(electrical, if (soft) collectorRollers.cargoReleaseSpeed / 2 else collectorRollers.cargoReleaseSpeed)
    } finally {
        scope.launch { collectorRollers?.set(collectorRollers.cargoState) }
    }
}

suspend fun Subsystems.pivotDown() {
    collectorPivot?.set(CollectorPivotState.Down)
    freeze()
}

suspend fun Subsystems.deployPanel() = supervisorScope {
    //Eject panel
    val hookSliderOut = launch { hookSlider?.set(HookSliderState.Out) }
    scope.launch { collectorRollers?.set(collectorRollers.hatchState) }

    try {
        freeze()
    } finally {
        withContext(NonCancellable) {
            val hookDown = launch { hook?.set(HookPosition.Down) }
            withTimeout(0.5.Second) {
                lift?.set(lift.hardware.position.optimizedRead(
                        currentTime, 0.Second
                ).y - 3.5.Inch, 0.5.Inch)
            }
            hookSliderOut.cancel()
            delay(0.2.Second)
            hookDown.cancel()
        }
    }
}

suspend fun Subsystems.collectCargo() = supervisorScope {
    //lift, collector down
    launch { lift?.set(lift.collectCargo, 0.Inch) }
    launch { collectorPivot?.set(CollectorPivotState.Down) }

    //Start rollers
    launch { handoffRollers?.spin(handoffRollers.cargoCollectSpeed) }
    launch { collectorRollers?.spin(electrical, collectorRollers.cargoCollectSpeed) }

    //Center slider
    collectorSlider?.set(0.Inch, electrical)

    //Set handoff pivot down
    withTimeout(1.Second) { handoffPivot?.set(handoffPivot.collectPosition, 10.Degree) }
    val handoffPivotSet = launch { handoffPivot?.set(handoffPivot.collectPosition, 0.Degree) }

    try {
        freeze()
    } finally {
        scope.launch { collectorRollers?.set(collectorRollers.cargoState) }
        withContext(NonCancellable) {
            withTimeout(1.Second) {
                handoffPivotSet.join()
            }
        }
    }
}

//suspend fun Subsystems.collectPanel() = coroutineScope {
//    //Lift down
//    withTimeout(1.Second) { lift?.set(lift.collectPanel + 2.Inch, 1.Inch) }
//    launch { lift?.set(lift.collectPanel + 2.Inch, 0.Inch) }
//
//    //Hook down, slider out
//    val hookDown = scope.launch { hook?.set(HookPosition.Down) }
//    val hookSliderOut = scope.launch { hookSlider?.set(HookSliderState.Out) }
//
//    try {
//        freeze()
//    } finally {
//        withContext(NonCancellable) {
//            hookDown.cancel()
//            withTimeout(0.5.Second) {
//                lift?.set(lift.collectPanel + lift.collectPanelStroke, 1.Inch)
//            }
//            hookSliderOut.cancel()
//        }
//    }
//}

suspend fun Subsystems.lilDicky() = coroutineScope {
    //Lift down
    withTimeout(1.Second) { lift?.set(lift.collectPanel, 1.Inch) }
    launch { lift?.set(lift.collectPanel, 0.Inch) }

    //Hook down, slider out
    val hookDown = scope.launch { hook?.set(HookPosition.Down) }
    scope.launch { collectorRollers?.set(collectorRollers.hatchState) }

    try {
        freeze()
    } finally {
        withContext(NonCancellable) {
            val hookSliderOut = launch { hookSlider?.set(HookSliderState.Out) }
            delay(0.2.Second)
            hookDown.cancel()
            withTimeout(0.2.Second) {
                lift?.set(lift.collectPanel + lift.collectPanelStroke, 1.Inch)
            }
            launch { lift?.set(lift.collectPanel + lift.collectPanelStroke, 0.Inch) }
            hookSliderOut.cancel()
        }
    }
}

suspend fun Subsystems.trackLine() = coroutineScope {
    //Track line with slider
    collectorSlider?.trackLine(lineScanner, electrical)
    freeze()
}

suspend fun Subsystems.centerAll() = coroutineScope {
    centerSlider(0.Inch)
//    launch { centerCargo() }
    freeze()
}

suspend fun Subsystems.centerSlider(tolerance: Length = 1.Inch) {
    collectorSlider?.set(
            0.Inch,
            electrical,
            tolerance
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

