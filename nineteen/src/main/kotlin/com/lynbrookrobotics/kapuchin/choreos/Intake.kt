package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {

    val deployCargo by operator.deployCargo.readEagerly().withoutStamps
    val deployPanel by operator.deployPanel.readEagerly().withoutStamps
    val collectCargo by operator.collectCargo.readEagerly().withoutStamps
    val collectPanel by operator.collectPanel.readEagerly().withoutStamps
    val lineTracking by operator.lineTracking.readEagerly().withoutStamps
    val centerSlider by operator.centerSlider.readEagerly().withoutStamps
    val centerCargo by operator.centerCargo.readEagerly().withoutStamps
    val sliderPrecision by operator.sliderPrecision.readEagerly().withoutStamps

    choreography {
        whenever({ deployCargo || deployPanel || collectCargo || collectPanel || lineTracking || centerCargo || sliderPrecision != 0.0 }) {
            runWhile({ deployCargo }) { deployCargo() }
            runWhile({ deployPanel }) { deployPanel() }
            runWhile({ collectCargo }) { collectCargo() }
            runWhile({ collectPanel }) { collectPanel() }
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
}

suspend fun Subsystems.deployPanel() {
    //Eject panel
    hookSlider?.set(HookSliderState.Out)
    hook?.set(HookPosition.Down)
}

suspend fun Subsystems.collectCargo() = coroutineScope {
    try {
        //Start rollers
        launch { handoffRollers?.spin(handoffRollers.cargoCollectSpeed) }
        launch { collectorRollers?.spin(electrical, collectorRollers.cargoCollectSpeed) }

        //Center slider
        collectorSlider?.set(0.Inch, electrical)

        //Set handoff pivot down
        handoffPivot?.set(handoffPivot.handoffPosition)

        //lift, collector down
        launch { lift?.set(lift.collectCargo, 0.Inch) }
        launch { collectorPivot?.set(CollectorPivotState.Down) }

        freeze()
    } finally {
        handoffPivot?.set(handoffPivot.collectPosition)
    }
}

suspend fun Subsystems.collectPanel() = coroutineScope {
    //Center slider
    collectorSlider?.set(0.Inch, electrical)

    //Lift down
    lift?.set(lift.collectPanel, 0.Inch)

    //Hook down, slider out
    launch { hook?.set(HookPosition.Down) }
    launch { hookSlider?.set(HookSliderState.Out) }
}

suspend fun Subsystems.lineTracking() = coroutineScope {
    //Track line with slider
    if (lineScanner != null) {
        collectorSlider?.trackLine(0.5.Inch, lineScanner, electrical)
    }
}

suspend fun Subsystems.centerSlider() = collectorSlider?.set(
        0.Inch,
        electrical
) ?: freeze()

suspend fun Subsystems.centerCargo() = collectorRollers?.spin(
        electrical,
        collectorRollers.cargoCenterSpeed, // bottom out
        -collectorRollers.cargoCenterSpeed // top in
) ?: freeze()

suspend fun Subsystems.sliderPrecision(target: DutyCycle) = collectorSlider?.set(target) ?: freeze()

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

//suspend fun Subsystems.collectGroundPanel() = coroutineScope {
//    try {
//        //Center slider
//        collectorSlider?.set(0.Inch, electrical)
//
//        //Lift down
//        launch { lift?.set(lift.collectGroundPanel) }
//
//        //Handoff, velcro, hook down
//        handoffPivot?.set(handoffPivot.collectPosition)
//        velcroPivot?.set(VelcroPivotPosition.Down)
//        hook?.set(HookPosition.Down)
//
//        freeze()
//    } finally {
//        //Handoff, hook up
//        handoffPivot?.set(handoffPivot.handoffPosition)
//    }
//}
//
