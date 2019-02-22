package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

suspend fun intakeTeleop(
        driver: DriverHardware,
        oper: OperatorHardware,
        lineScanner: LineScannerHardware,
        collectorPivot: CollectorPivotComponent,
        collectorRollers: CollectorRollersComponent,
        collectorSlider: CollectorSliderComponent,
        hook: HookComponent,
        hookSlider: HookSliderComponent,
        handoffPivot: HandoffPivotComponent,
        handoffRollers: HandoffRollersComponent,
        velcroPivot: VelcroPivotComponent,
        lift: LiftComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Intake teleop") {

    val collectCargo by driver.collectCargo.readEagerly().withoutStamps
    val collectWallPanel by driver.collectWallPanel.readEagerly().withoutStamps
    val collectGroundPanel by driver.collectGroundPanel.readEagerly().withoutStamps
    val deployCargo by oper.deployCargo.readEagerly().withoutStamps
    val deployPanel by oper.deployPanel.readEagerly().withoutStamps
    val pushPanel by oper.pushPanel.readEagerly().withoutStamps

    choreography {
        whenever({isActive}) {
            runWhile({collectCargo}) {
                collectCargo(collectorPivot, collectorRollers, collectorSlider, handoffPivot, handoffRollers, lift, electricalSystem)
            }
            runWhile({collectWallPanel}) {
                collectWallPanel(lineScanner, collectorPivot, collectorSlider, handoffPivot, hook, hookSlider, lift, electricalSystem)
            }
            runWhile({collectGroundPanel}) {
                collectGroundPanel(collectorPivot, collectorSlider, handoffPivot, velcroPivot, hook, lift, electricalSystem)
            }
            runWhile({deployCargo}) {
                deployCargo(lineScanner, collectorPivot, collectorRollers, collectorSlider, electricalSystem)
            }
            runWhile({deployPanel}) {
                deployPanel(lineScanner, collectorPivot, collectorSlider, hook, hookSlider, electricalSystem)
            }
            runWhile({pushPanel}) {
                pushPanel(collectorPivot, hook, hookSlider)
            }
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
suspend fun collectCargo(
        collectorPivot: CollectorPivotComponent,
        collectorRollers: CollectorRollersComponent,
        collectorSlider: CollectorSliderComponent,
        handoffPivot: HandoffPivotComponent,
        handoffRollers: HandoffRollersComponent,
        lift: LiftComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Collect cargo") {

    choreography {
        //Start collector/handoff rollers
        launch { handoffRollers.spin(handoffRollers.cargoCollectSpeed) }
        launch { collectorRollers.spin(collectorRollers.cargoCollectSpeed) }

        //Center slider
        collectorSlider.set(0.Inch, electricalSystem)

        //lift, handoff, collector down
        lift.set(lift.collectCargo)
        collectorPivot.set(CollectorPivotPosition.Down)
        handoffPivot.set(handoffPivot.collectPosition)

        //Wait (for cargo to be collected)
        delay(0.5.Second)

        //Collector, handoff up
        collectorPivot.set(CollectorPivotPosition.Up)
        handoffPivot.set(handoffPivot.collectPosition)
    }
}


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
suspend fun collectWallPanel(
        lineScanner: LineScannerHardware,
        collectorPivot: CollectorPivotComponent,
        collectorSlider: CollectorSliderComponent,
        handoffPivot: HandoffPivotComponent,
        hook: HookComponent,
        hookSlider: HookSliderComponent,
        lift: LiftComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Collect wall panel") {

    choreography {
        //Track line with slider
        collectorSlider.trackLine(0.5.Inch, lineScanner, electricalSystem)

        //Lift down
        lift.set(lift.collectPanel)

        //Handoff, collector up
        handoffPivot.set(handoffPivot.handoffPosition)
        collectorPivot.set(CollectorPivotPosition.Up)

        //Hook down, slider out
        hook.set(HookPosition.Down)
        hookSlider.set(HookSliderPosition.Out)

        //Hook up, slider in
        hook.set(HookPosition.Up)
        hookSlider.set(HookSliderPosition.In)
    }
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
suspend fun collectGroundPanel(
        collectorPivot: CollectorPivotComponent,
        collectorSlider: CollectorSliderComponent,
        handoffPivot: HandoffPivotComponent,
        velcroPivot: VelcroPivotComponent,
        hook: HookComponent,
        lift: LiftComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Collect ground panel") {

    choreography {
        //Center slider
        collectorSlider.set(0.Inch, electricalSystem)

        //Lift down
        lift.set(lift.collectGroundPanel)

        //Collector up
        collectorPivot.set(CollectorPivotPosition.Up)

        //Handoff, velcro, hook down
        handoffPivot.set(handoffPivot.collectPosition)
        velcroPivot.set(VelcroPivotPosition.Down)
        hook.set(HookPosition.Down)

        //Handoff, hook up
        handoffPivot.set(handoffPivot.handoffPosition)
        hook.set(HookPosition.Up)
    }
}


/**
 * Deploy cargo with slider autoalign
 *
 * Ends with:
 * CollectorPivot - Up
 * CollectorSlider - Random
 */
suspend fun deployCargo(
        lineScanner: LineScannerHardware,
        collectorPivot: CollectorPivotComponent,
        collectorRollers: CollectorRollersComponent,
        collectorSlider: CollectorSliderComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Deploy cargo") {

    choreography {
        //Collector up
        collectorPivot.set(CollectorPivotPosition.Up)

        //Track line with slider
        collectorSlider.trackLine(0.5.Inch, lineScanner, electricalSystem)

        //Spin rollers
        collectorRollers.spin(collectorRollers.cargoReleaseSpeed)
    }
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
suspend fun deployPanel(
        lineScanner: LineScannerHardware,
        collectorPivot: CollectorPivotComponent,
        collectorSlider: CollectorSliderComponent,
        hook: HookComponent,
        hookSlider: HookSliderComponent,
        electricalSystem: ElectricalSystemHardware
) = startChoreo("Deploy panel") {

    choreography {
        //Collector up
        collectorPivot.set(CollectorPivotPosition.Up)

        //Track line with slider
        collectorSlider.trackLine(0.5.Inch, lineScanner, electricalSystem)

        //Eject panel
        hookSlider.set(HookSliderPosition.Out)
        hook.set(HookPosition.Down)

        //Reset hook, hook slider
        hookSlider.set(HookSliderPosition.In)
        hook.set(HookPosition.Up)
    }
}


/**
 * Deploy panel WITHOUT slider autoalign
 *
 * Ends with:
 * CollectorPivot - Up
 * Hook - Down
 * HookSlider - In
 */
suspend fun pushPanel(
        collectorPivot: CollectorPivotComponent,
        hook: HookComponent,
        hookSlider: HookSliderComponent
) = startChoreo("Push panel") {

    choreography {
        //Collector up
        collectorPivot.set(CollectorPivotPosition.Up)

        //Eject panel
        hookSlider.set(HookSliderPosition.Out)
        hook.set(HookPosition.Down)

        //Reset hook, hook slider
        hookSlider.set(HookSliderPosition.In)
        hook.set(HookPosition.Up)
    }
}
