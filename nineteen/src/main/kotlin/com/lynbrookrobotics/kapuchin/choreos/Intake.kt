package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.HookPosition.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.HookSliderState.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

suspend fun Subsystems.intakeTeleop() = startChoreo("Intake teleop") {

    val collectCargo by driver.collectCargo.readEagerly().withoutStamps
    val centerCargo by operator.centerCargo.readEagerly().withoutStamps
    val collectWallPanel by driver.collectWallPanel.readEagerly().withoutStamps
    val collectGroundPanel by driver.collectGroundPanel.readEagerly().withoutStamps
    val deployCargo by operator.deployCargo.readEagerly().withoutStamps
    val deployPanel by operator.deployPanel.readEagerly().withoutStamps
    val pushPanel by operator.pushPanel.readEagerly().withoutStamps

    choreography {
        whenever({ collectCargo || centerCargo || collectWallPanel || collectGroundPanel || deployCargo || deployPanel || pushPanel }) {
            runWhile({ collectCargo }) { collectCargo() }
            runWhile({ centerCargo }) { centerCargo() }
            runWhile({ collectWallPanel }) { collectWallPanel() }
            runWhile({ collectGroundPanel }) { collectGroundPanel() }
            runWhile({ deployCargo }) { deployCargo() }
            runWhile({ deployPanel }) { deployPanel() }
            runWhile({ pushPanel }) { pushPanel() }
        }
    }
}

/**
 * Collect cargo from loading station
 *
 * Ends with:
 * Ball in collector
 * CollectorPivot - Up
 * CollectorSlider - Center
 * HandoffPivot - Collect position
 * Lift - Collect cargo height
 */
suspend fun Subsystems.collectCargo() = coroutineScope {
    //Start collector/handoff rollers
    try {
        launch { handoffRollers?.spin(handoffRollers.cargoCollectSpeed) }
        launch { collectorRollers?.spin(electrical, collectorRollers.cargoCollectSpeed) }

        //Center slider
        collectorSlider?.set(0.Inch, electrical)

        //lift, handoff, collector down
        launch { lift?.set(lift.collectCargo, 0.Inch) }
        launch { collectorPivot?.set(CollectorPivotState.Down) }

        freeze()
    } finally {
        handoffPivot?.set(handoffPivot.collectPosition)
    }
}

/**
 * Center cargo with rollers
 */
suspend fun Subsystems.centerCargo() = collectorRollers?.spin(electrical,
        collectorRollers.cargoCenterSpeed, // bottom out
        -collectorRollers.cargoCenterSpeed // top in
) ?: freeze()

/**
 * Collect panel from loading station
 *
 * End with:
 * Panel on hook
 * CollectorPivot - Up
 * CollectorSlider - Random
 * HandoffPivot - Handoff Position
 * Hook - Up
 * HookSlider - In
 * Lift - Collect panel height
 */
suspend fun Subsystems.collectWallPanel() = coroutineScope {
    //Track line with slider
    if (lineScanner != null) {
        collectorSlider?.trackLine(0.5.Inch, lineScanner, electrical)
    }

    //Lift down
    launch { lift?.set(lift.collectPanel, 0.Inch) }

    //Handoff, collector up
    handoffPivot?.set(handoffPivot.handoffPosition)

    //Hook down, slider out
    launch { hook?.set(HookPosition.Down) }
    launch { hookSlider?.set(HookSliderState.Out) }
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
suspend fun Subsystems.collectGroundPanel() {
    try {
        //Center slider
        collectorSlider?.set(0.Inch, electrical)

        //Lift down
        launch { lift?.set(lift.collectGroundPanel) }

        //Handoff, velcro, hook down
        handoffPivot?.set(handoffPivot.collectPosition)
        velcroPivot?.set(VelcroPivotPosition.Down)
        hook?.set(HookPosition.Down)

        freeze()
    } finally {
        //Handoff, hook up
        handoffPivot?.set(handoffPivot.handoffPosition)
    }
}

/**
 * Deploy cargo with slider autoalign
 *
 * Ends with:
 * CollectorPivot - Up
 * CollectorSlider - Random
 */
suspend fun Subsystems.deployCargo() {
    //Track line with slider
    if (lineScanner != null) {
        collectorSlider?.trackLine(0.5.Inch, lineScanner, electrical)
    }

    //Spin rollers
    collectorRollers?.spin(electrical, collectorRollers.cargoReleaseSpeed)
}

/**
 * Deploy panel with slider autoalign
 *
 * Ends with:
 * CollectorPivot - Up
 * CollectorSlider - Random
 * Hook - Up
 * HookSlider - In
 */
suspend fun Subsystems.deployPanel() {
    //Collector up
    collectorPivot?.set(CollectorPivotState.Up)

    //Track line with slider
    if (lineScanner != null) {
        collectorSlider?.trackLine(0.5.Inch, lineScanner, electrical)
    }

    //Eject panel
    hookSlider?.set(HookSliderState.Out)
    hook?.set(HookPosition.Down)
}

/**
 * Deploy panel WITHOUT slider autoalign
 *
 * Ends with:
 * CollectorPivot - Up
 * Hook - Down
 * HookSlider - In
 */
suspend fun Subsystems.pushPanel() {
    //Collector up
    collectorPivot?.set(CollectorPivotState.Up)

    //Eject panel
    hookSlider?.set(HookSliderState.Out)
    hook?.set(HookPosition.Down)
}