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


    //TODO: goToLeftFrontCargo
    //TODO: goToRightFrontCargo

    val goToLeftCloseCargo by driver.goToLeftCloseCargo.readEagerly().withoutStamps
    val goToRightCloseCargo by driver.goToRightCloseCargo.readEagerly().withoutStamps

    val goToLeftMiddleCargo by driver.goToLeftMiddleCargo.readEagerly().withoutStamps
    val goToRightMiddleCargo by driver.goToRightMiddleCargo.readEagerly().withoutStamps

    val goToLeftFarCargo by driver.goToLeftFarCargo.readEagerly().withoutStamps
    val goToRightFarCargo by driver.goToRightFarCargo.readEagerly().withoutStamps

    val goToLeftCloseRocket by driver.goToLeftCloseRocket.readEagerly().withoutStamps
    val goToRightCloseRocket by driver.goToRightCloseRocket.readEagerly().withoutStamps

    //TODO: goToLeftMiddleRocket
    //TODO: goToRightMiddleRocket

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
            runWhile({goToRightLoadingStation}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), rightLoadingStation, 0.5.Inch)
            }
            runWhile({goToRightCloseCargo}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), rightCloseCargo, 0.5.Inch)
            }
            runWhile({goToLeftCloseCargo}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), leftCloseCargo, 0.5.Inch)
            }
            runWhile({goToRightCloseCargo}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), rightCloseCargo, 0.5.Inch)
            }
            runWhile({goToLeftMiddleCargo}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), leftMiddleCargo, 0.5.Inch)
            }
            runWhile({goToRightMiddleCargo}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), rightMiddleCargo, 0.5.Inch)
            }
            runWhile({goToLeftFarCargo}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), rightFarCargo, 0.5.Inch)
            }
            runWhile({goToRightFarCargo}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), rightFarCargo, 0.5.Inch)
            }
            runWhile({goToLeftCloseRocket}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), leftCloseRocket, 0.5.Inch)
            }
            runWhile({goToRightCloseRocket}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), rightCloseRocket, 0.5.Inch)
            }
            runWhile({goToLeftFarRocket}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), leftFarRocket, 0.5.Inch)
            }
            runWhile({goToRightFarRocket}) {
                drivetrain.waypoint(trapezoidalMotionProfile(
                        0.5.FootPerSecondSquared,
                        3.FootPerSecond
                ), rightFarRocket, 0.5.Inch)
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