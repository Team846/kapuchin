package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.ThrustmasterButtons.*
import com.lynbrookrobotics.kapuchin.routines.*
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.isActive

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {


    fun zero(location: UomVector<Length>) {
        drivetrain.hardware.conversions.matrixTracking.x = location.x
        drivetrain.hardware.conversions.matrixTracking.y = location.y
     }

    val visionAlign by driver.lineTracking.readEagerly().withoutStamps


    //go-to functions

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


    //zero-at functions

    val zeroAtLeftLoadingStation by driver.zeroAtLeftLoadingStation.readEagerly().withoutStamps
    val zeroAtRightLoadingStation by driver.zeroAtRightLoadingStation.readEagerly().withoutStamps

    //TODO: zeroAtLeftFrontCargo
    //TODO: zeroAtRightFrontCargo

    val zeroAtLeftCloseCargo by driver.zeroAtLeftCloseCargo.readEagerly().withoutStamps
    val zeroAtRightCloseCargo by driver.zeroAtRightCloseCargo.readEagerly().withoutStamps

    val zeroAtLeftMiddleCargo by driver.zeroAtLeftMiddleCargo.readEagerly().withoutStamps
    val zeroAtRightMiddleCargo by driver.zeroAtRightMiddleCargo.readEagerly().withoutStamps

    val zeroAtLeftFarCargo by driver.zeroAtLeftFarCargo.readEagerly().withoutStamps
    val zeroAtRightFarCargo by driver.zeroAtRightFarCargo.readEagerly().withoutStamps

    val zeroAtLeftCloseRocket by driver.zeroAtLeftCloseRocket.readEagerly().withoutStamps
    val zeroAtRightCloseRocket by driver.zeroAtRightCloseRocket.readEagerly().withoutStamps

    //TODO: zeroAtLeftMiddleRocket
    //TODO: zeroAtRightMiddleRocket

    val zeroAtLeftFarRocket by driver.zeroAtLeftFarRocket.readEagerly().withoutStamps
    val zeroAtRightFarRocket by driver.zeroAtRightFarRocket.readEagerly().withoutStamps

    

    choreography {
        while (isActive) {
            runWhile({goToLeftLoadingStation}) {

            }
            runWhile({goToRightLoadingStation}) {

            }
            runWhile({goToRightCloseCargo}) {

            }
            runWhile({goToLeftCloseCargo}) {

            }
            runWhile({goToRightCloseCargo}) {

            }
            runWhile({goToLeftMiddleCargo}) {

            }
            runWhile({goToRightMiddleCargo}) {

            }
            runWhile({goToLeftFarCargo}) {

            }
            runWhile({goToRightFarCargo}) {

            }
            runWhile({goToLeftCloseRocket}) {

            }
            runWhile({goToRightCloseRocket}) {

            }
            runWhile({goToLeftFarRocket}) {

            }
            runWhile({goToRightFarRocket}) {

            }
            runWhile({zeroAtLeftLoadingStation}) {
                zero(leftLoadingStation)
            }
            runWhile({zeroAtRightLoadingStation}) {
                zero(rightLoadingStation)
            }
            runWhile({zeroAtRightCloseCargo}) {
                zero(rightCloseCargo)
            }
            runWhile({zeroAtLeftCloseCargo}) {
                zero(leftCloseCargo)
            }
            runWhile({zeroAtRightCloseCargo}) {
                zero(rightCloseCargo)
            }
            runWhile({zeroAtLeftMiddleCargo}) {
                zero(leftMiddleCargo)
            }
            runWhile({zeroAtRightMiddleCargo}) {
                zero(rightMiddleCargo)
            }
            runWhile({zeroAtLeftFarCargo}) {
                zero(leftFarCargo)
            }
            runWhile({zeroAtRightFarCargo}) {
                zero(rightFarCargo)
            }
            runWhile({zeroAtLeftCloseRocket}) {
                zero(leftCloseRocket)
            }
            runWhile({zeroAtRightCloseRocket}) {
                zero(rightCloseRocket)
            }
            runWhile({zeroAtLeftFarRocket}) {
                zero(leftFarRocket)
            }
            runWhile({zeroAtRightFarRocket}) {
                zero(rightFarRocket)
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