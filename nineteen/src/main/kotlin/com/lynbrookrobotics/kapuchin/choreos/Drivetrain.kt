package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.ThrustmasterButtons.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.isActive

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {

    val visionAlign by driver.lineTracking.readEagerly().withoutStamps

    val goToLeftLoadingStation by driver.goToLeftLoadingStation.readEagerly().withoutStamps
    val goToRightLoadingStation by driver.goToRightLoadingStation.readEagerly().withoutStamps


    val goToLeftCloseCargo by driver.goToLeftCloseCargo.readEagerly().withoutStamps
    val goToRightCloseCargo by driver.goToRightCloseCargo.readEagerly().withoutStamps

    val goToLeftMiddleCargo by driver.goToLeftMiddleCargo.readEagerly().withoutStamps
    val goToRightMiddleCargo by driver.goToRightMiddleCargo.readEagerly().withoutStamps

    val goToLeftFarCargo by driver.goToLeftFarCargo.readEagerly().withoutStamps
    val goToRightFarCargo by driver.goToRightFarCargo.readEagerly().withoutStamps


    val goToLeftCloseRocket by driver.goToLeftCloseRocket.readEagerly().withoutStamps
    val goToRightCloseRocket by driver.goToRightCloseRocket.readEagerly().withoutStamps

    val goToLeftFarRocket by driver.goToLeftFarRocket.readEagerly().withoutStamps
    val goToRightFarRocket by driver.goToRightFarRocket.readEagerly().withoutStamps

    choreography {
        while (isActive) {
            runWhile({goToLeftLoadingStation}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), leftLoadingStation, 0.5.Inch)
            }
            runWhile({ !visionAlign }) {
                drivetrain.teleop(driver)
            }
            runWhile({ visionAlign }) {
                if (limelight != null) {
                    drivetrain.visionSnapshotTracking(5.FootPerSecond, limelight)
                }
                freeze()
            }
        }
    }
}