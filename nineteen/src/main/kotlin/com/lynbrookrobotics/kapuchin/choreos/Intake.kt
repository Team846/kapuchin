package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {

    val deployCargo by operator.deployCargo.readEagerly().withoutStamps
    val deployPanel by operator.deployPanel.readEagerly().withoutStamps
    val collectCargo by operator.collectCargo.readEagerly().withoutStamps
    val collectPanel by operator.collectPanel.readEagerly().withoutStamps
    val collectGroundPanel by operator.collectGroundPanel.readEagerly().withoutStamps
    val lineTracking by operator.lineTracking.readEagerly().withoutStamps
    val centerSlider by operator.centerSlider.readEagerly().withoutStamps
    val centerCargo by operator.centerCargo.readEagerly().withoutStamps
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps

    choreography {
        whenever({ deployCargo || deployPanel || collectCargo || collectGroundPanel || lineTracking || centerCargo || sliderPrecision != 0.0 }) {
            runWhile({ deployCargo }) { deployCargo() }
            runWhile({ deployPanel }) { deployPanel() }
            runWhile({ collectCargo }) { collectCargo() }
            runWhile({ collectPanel }) { collectPanel() }
            runWhile({ collectGroundPanel }) { collectGroundPanel() }
            runWhile({ lineTracking }) { lineTracking() }
            runWhile({ centerSlider }) { centerSlider() }
            runWhile({ centerCargo }) { centerCargo() }
            runWhile({ sliderPrecision != 0.0 }) { sliderPrecision(sliderPrecision.Each) }
        }
    }
}

suspend fun Subsystems.deployCargo() {
    //Center collector slider
    collectorSlider?.set(0.Inch, electrical)

    //Eject cargo
    collectorRollers?.spin(electrical, collectorRollers.cargoReleaseSpeed)
    freeze()
}

suspend fun Subsystems.deployPanel() = coroutineScope {
    //Eject panel
    val j1 = scope.launch { hookSlider?.set(HookSliderState.Out) }
    delay(0.5.Second)
    val j2 = scope.launch { hook?.set(HookPosition.Down) }

    try {
        freeze()
    } finally {
        withContext(NonCancellable) {
            j1.cancel()
            delay(0.5.Second)
            j2.cancel()
        }
    }
}

suspend fun Subsystems.collectCargo() = coroutineScope {
    try {
        //Start rollers
        launch { handoffRollers?.spin(handoffRollers.cargoCollectSpeed) }
        launch { collectorRollers?.spin(electrical, collectorRollers.cargoCollectSpeed) }

        //Center slider
        collectorSlider?.set(0.Inch, electrical)

        //Set handoff pivot down
        handoffPivot?.set(handoffPivot.collectPosition)

        //lift, collector down
        launch { lift?.set(lift.collectCargo, 0.Inch) }
        launch { collectorPivot?.set(CollectorPivotState.Down) }

        freeze()
    } finally {
        withContext(NonCancellable) {
            handoffPivot?.set(handoffPivot.handoffPosition)
        }
    }
}

suspend fun Subsystems.collectPanel() = coroutineScope {
    //Center slider
    collectorSlider?.set(0.Inch, electrical)

    //Lift down
    lift?.set(lift.collectPanel, 1.Inch)
    launch { lift?.set(lift.collectPanel, 0.Inch) }

    //Hook down, slider out
    launch { hook?.set(HookPosition.Down) }
    launch { hookSlider?.set(HookSliderState.Out) }
    freeze()
}

suspend fun Subsystems.lineTracking() = coroutineScope {
    //Track line with slider
    if (lineScanner != null) {
        collectorSlider?.trackLine(0.5.Inch, lineScanner, electrical)
    }
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
            collectorRollers.cargoCenterSpeed, // bottom out
            -collectorRollers.cargoCenterSpeed // top in
    )
    freeze()
}

suspend fun Subsystems.sliderPrecision(target: DutyCycle) {
    collectorSlider?.set(target)
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
    try {
        //Center slider
        collectorSlider?.set(0.Inch, electrical)

        //Lift down
        launch { lift?.set(lift.collectGroundPanel) }

        //Handoff, velcro, hook down
        launch { handoffPivot?.set(handoffPivot.collectPosition) }
        launch { velcroPivot?.set(VelcroPivotPosition.Down) }
        launch { hook?.set(HookPosition.Down) }

        freeze()
    } finally {
        withContext(NonCancellable) {
            //Handoff, hook up
            handoffPivot?.set(handoffPivot.handoffPosition)
        }
    }
}

