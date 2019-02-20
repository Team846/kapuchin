package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.isActive

suspend fun drivetrainTeleop(
        drivetrain: DrivetrainComponent,
        driver: DriverHardware,
        limelight: LimelightHardware
) = startChoreo("Teleop") {

    val visionAlign by driver.visionAlign.readEagerly().withoutStamps

    choreography {
        while (isActive) {
            runWhile({ !visionAlign }) {
                drivetrain.teleop(driver)
            }
            runWhile({ visionAlign }) {
                limelightAlign(drivetrain, limelight)
            }
        }
    }
}

suspend fun liftTeleop(
        lift: LiftComponent,
        oper: OperatorHardware
) = startChoreo("Teleop") {

    val groundHeight by oper.groundHeight.readEagerly().withoutStamps
    val collectGroundPanel by oper.collectGroundPanel.readEagerly().withoutStamps

    val lowPanelHeight by oper.lowPanelHeight.readEagerly().withoutStamps
    val lowCargoHeight by oper.lowCargoHeight.readEagerly().withoutStamps

    val midPanelHeight by oper.midPanelHeight.readEagerly().withoutStamps
    val midCargoHeight by oper.midCargoHeight.readEagerly().withoutStamps

    val highPanelHeight by oper.highPanelHeight.readEagerly().withoutStamps
    val highCargoHeight by oper.highCargoHeight.readEagerly().withoutStamps

    choreography {
        while (isActive) {
            runWhile({ groundHeight }) {
                lift.to(lift.collectHeight, 0.Inch)
            }
            runWhile({ collectGroundPanel }) {
                lift.to(lift.collectGroundPanel, 0.Inch)
            }
            runWhile({ lowPanelHeight }) {
                lift.to(lift.panelLowRocket, 0.Inch)
            }
            runWhile({ lowCargoHeight }) {
                lift.to(lift.cargoLowRocket, 0.Inch)
            }
            runWhile({ midPanelHeight }) {
                lift.to(lift.panelMidRocket, 0.Inch)
            }
            runWhile({ midCargoHeight }) {
                lift.to(lift.cargoMidRocket, 0.Inch)
            }
            runWhile({ highPanelHeight }) {
                lift.to(lift.panelHighRocket, 0.Inch)
            }
            runWhile({ highCargoHeight }) {
                lift.to(lift.cargoHighRocket, 0.Inch)
            }
        }
    }
}
