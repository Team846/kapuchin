package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.launch

/**
 * Collect cargo from loading station
 *
 * Ends with:
 * Ball in collector
 * CollectorPivot - Up
 * HandoffPivot - Collect position
 * Lift - Collect cargo height
 */
suspend fun collectCargo(
        collectorPivot: CollectorPivotComponent,
        collectorRollers: CollectorRollersComponent,
        handoffPivot: HandoffPivotComponent,
        handoffRollers: HandoffRollersComponent,
        lift: LiftComponent
) = startChoreo("Collect cargo") {

    choreography {
        //Start collector/handoff rollers
        val hr = launch { handoffRollers.spin(handoffRollers.cargoCollectSpeed) }
        launch { collectorRollers.spin(collectorRollers.cargoCollectSpeed) }

        //Set up pivots and lift
        launch {
            collectorPivot.set(CollectorPivotPosition.Up)
            handoffPivot.set(handoffPivot.collectPosition)
            lift.set(lift.collectCargo)
        }

        //Wait (for cargo to be collected) then stop handoff rollers
        delay(0.5.Second)
        hr.cancel()

        //Handoff cargo to collector
        launch {
            handoffPivot.set(handoffPivot.handoffPosition)
            collectorPivot.set(CollectorPivotPosition.Down)
        }

        //Wait (for handoff to be complete) then reset pivots
        delay(0.3.Second)
        launch {
            handoffPivot.set(handoffPivot.collectPosition)
            collectorPivot.set(CollectorPivotPosition.Up)
        }
    }
}


/**
 * Collect panel from loading station
 *
 * End with:
 * Panel on hook
 * Hook - Up
 * HookSlider - In
 * Lift - collectPanelHeight
 */
suspend fun collectWallPanel(
        lineScanner: LineScannerHardware,
        collectorSlider: CollectorSliderComponent,
        hook: HookComponent,
        hookSlider: HookSliderComponent,
        lift: LiftComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Collect wall panel") {

    choreography {
        //Start moving slider, set lift height, and hook/hook slider
        launch {
            collectorSlider.trackLine(0.5.Inch, lineScanner, electricalSystem)
            lift.set(lift.collectPanel)
            hook.set(HookPosition.Down)
            hookSlider.set(HookSliderPosition.Out)
        }

        //Collect panel and bring slider in
        hook.set(HookPosition.Up)
        hookSlider.set(HookSliderPosition.In)
    }
}


/**
 * Collect panel from the ground
 */
suspend fun collectGroundPanel(
        collectorSlider: CollectorSliderComponent,
        handoffPivot: HandoffPivotComponent,
        hook: HookComponent,
        hookSlider: HookSliderComponent,
        lift: LiftComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Collect ground panel") {
    //elevator handoff pos
////IH_pivot *down* (not coll) pos
////_wait for driver input_
////IH_velcroSol out (and stay out)
////IH_hookSol down
////IH_pivot *handoffpos*
    choreography {
        //TODO finish collect ground panel
    }
}


/**
 * Deploy cargo with slider autoalign
 */
suspend fun deployCargo(
        lineScanner: LineScannerHardware,
        collectorRollers: CollectorRollersComponent,
        collectorSlider: CollectorSliderComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Deploy cargo") {

    choreography {
        //Move slider then release
        collectorSlider.trackLine(0.5.Inch, lineScanner, electricalSystem)
        collectorRollers.spin(collectorRollers.cargoReleaseSpeed)
    }
}


/**
 * Deploy panel with slider autoalign
 *
 * Ends with:
 * Hook - Up
 * HookSlider - In
 * HatchPanelEjector - In
 */
suspend fun deployPanel(
        lineScanner: LineScannerHardware,
        collectorSlider: CollectorSliderComponent,
        hook: HookComponent,
        hookSlider: HookSliderComponent,
        hatchPanelEjector: HatchPanelEjectorComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Deploy panel") {

    choreography {
        //Move slider
        collectorSlider.trackLine(0.5.Inch, lineScanner, electricalSystem)

        //Set hook, hook slider
        launch {
            hookSlider.set(HookSliderPosition.Out)
            hook.set(HookPosition.Down)
        }

        //Eject panel
        hatchPanelEjector.set(EjectorState.Out)

        //Reset hook, hook slider, ejector
        launch {
            hook.set(HookPosition.Up)
            hookSlider.set(HookSliderPosition.In)
            hatchPanelEjector.set(EjectorState.In)
        }
    }
}


/**
 * Deploy panel WITHOUT slider autoalign
 *
 * Deploy p
 * Ends with:
 * Hook - Down
 * HookSlider - In
 * Lift - collectCargoHeight
 */
suspend fun pushPanel(
        hook: HookComponent,
        hookSlider: HookSliderComponent,
        hatchPanelEjector: HatchPanelEjectorComponent,
        ) = startChoreo("Push panel") {

    choreography {
        //Set hook, hook slider
        launch {
            hookSlider.set(HookSliderPosition.Out)
            hook.set(HookPosition.Down)
        }

        //Eject panel
        hatchPanelEjector.set(EjectorState.Out)

        //Reset hook, hook slider, ejector
        launch {
            hook.set(HookPosition.Up)
            hookSlider.set(HookSliderPosition.In)
            hatchPanelEjector.set(EjectorState.In)
        }
    }
}
