package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.pivot.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.*

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {

    val deployCargo by operator.deployCargo.readEagerly().withoutStamps
    val softDeployCargo by operator.softDeployCargo.readEagerly().withoutStamps
    val deployPanel by operator.deployPanel.readEagerly().withoutStamps
    val collectCargo by driver.collectCargo.readEagerly().withoutStamps
//    val panelCollect by operator.panelCollect.readEagerly().withoutStamps
    val lilDicky by operator.lilDicky.readEagerly().withoutStamps
//    val collectGroundPanel by operator.collectGroundPanel.readEagerly().withoutStamps
    val operatorLineTracking by operator.lineTracking.readEagerly().withoutStamps
//    val driverLineTracking by driver.lineTracking.readEagerly().withoutStamps
    val centerAll by operator.centerAll.readEagerly().withoutStamps
    val centerAllFlipped by operator.centerAllFlipped.readEagerly().withoutStamps
    val pivotDown by operator.pivotDown.readEagerly().withoutStamps
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps

    choreography {
        runWhenever(
                { deployCargo || softDeployCargo } to choreography { deployCargo(softDeployCargo) },
                { deployPanel } to choreography { deployPanel() },
                { collectCargo } to choreography { collectCargo() },
//                { panelCollect } to choreography { panelCollect() },
                { operatorLineTracking } to choreography { trackLine() },
                { lilDicky } to choreography { lilDicky() },
                { centerAll || centerAllFlipped } to choreography { centerAll(centerAllFlipped) },
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
    val hookSliderOut = scope.launch { hookSlider?.set(HookSliderState.Out) }
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
            delay(0.5.Second)
            hookDown.cancel()
        }
    }
}

suspend fun Subsystems.collectCargo() = supervisorScope {
    //lift, collector down
    lift?.set(lift.cargoCollect, 2.Inch)
    launch { lift?.set(lift.cargoCollect, 0.Inch) }

    launch { collectorPivot?.set(CollectorPivotState.Down) }

    //Start rollers
    launch { collectorRollers?.spin(electrical, collectorRollers.cargoCollectSpeed) }

    //Center slider
    collectorSlider?.set(0.Inch, electrical)

    try {
        freeze()
    } finally {
        scope.launch { collectorRollers?.set(collectorRollers.cargoState) }
    }
}

//suspend fun Subsystems.panelCollect() = coroutineScope {
//    //Lift down
//    withTimeout(1.Second) { lift?.set(lift.panelCollect + 2.Inch, 1.Inch) }
//    launch { lift?.set(lift.panelCollect + 2.Inch, 0.Inch) }
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
//                lift?.set(lift.panelCollectStroke, 1.Inch)
//            }
//            hookSliderOut.cancel()
//        }
//    }
//}

suspend fun Subsystems.lilDicky() = coroutineScope {
    //Lift down
    withTimeout(1.Second) { lift?.set(lift.panelCollect, 1.Inch) }
    launch { lift?.set(lift.panelCollect, 0.Inch) }

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
                lift?.set(lift.panelCollectStroke, 1.Inch)
            }
            scope.launch { lift?.set(lift.panelCollectStroke, 0.Inch) }
            hookSliderOut.cancel()
        }
    }
}

suspend fun Subsystems.trackLine() = coroutineScope {
    //Track line with slider
    collectorSlider?.trackLine(lineScanner, electrical)
    freeze()
}

suspend fun Subsystems.centerAll(flip: Boolean) = coroutineScope {
    launch { centerSlider(0.Inch) }
    launch { centerCargo(flip) }
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

suspend fun Subsystems.centerCargo(flip: Boolean) {
    try {
        collectorRollers?.spin(
                electrical,
                top = if (flip) -10.5.Volt else 11.5.Volt,
                bottom = if (flip) 11.5.Volt else -9.5.Volt
        )
    } finally {
        scope.launch { collectorRollers?.set(collectorRollers.cargoState) }
    }
}

