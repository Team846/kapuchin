package com.lynbrookrobotics.kapuchin.choreos

import com.lynbrookrobotics.kapuchin.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.*
import com.lynbrookrobotics.kapuchin.hardware.ThrustmasterButtons.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.timing.*
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.isActive

suspend fun Subsystems.drivetrainTeleop() = startChoreo("Drivetrain teleop") {


    fun zero(pos: Position) {
        lineScanner.linePosition.optimizedRead(currentTime, 0.Second).y.let {
            if (it != null) {
              val bumperToLineScanner = drivetrain.hardware.bumperAheadDistance - lineScanner.mounting.y - lineScanner.lookAhead

              val lineAngle = atan2(it, lineScanner.lookAhead)
              val lineDist = hypot(it, lineScanner.lookAhead)

              val lineScannerAngle = atan2(lineScanner.mounting.x, lineScanner.mounting.y)
              val lineScannerDistance = hypot(lineScanner.mounting.x, lineScanner.mounting.y)

              val robotPosition = pos.vector -
                UomVector(
                  bumperToLineScanner * sin(pos.bearing),
                  bumperToLineScanner * cos(pos.bearing)) -
                UomVector(
                  lineDist * sin(pos.bearing + lineAngle),
                  lineDist * cos(pos.bearing + lineAngle)) -
                UomVector(
                    lineScannerDistance * sin(pos.bearing + lineScannerAngle),
                    lineScannerDistance * cos(pos.bearing + lineScannerAngle)
                  )
            }
        }
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
        val motionProfile = trapezoidalMotionProfile(
                deceleration = 5.FootPerSecondSquared,
                topSpeed = drivetrain.maxSpeed
        )

        whenever({drivetrain.routine == null}) {
            runWhile({goToLeftLoadingStation}) {
                drivetrain.positionAndButtUp(motionProfile, leftLoadingStation, 0.5.Inch, 10.Degree)
            }
            runWhile({goToRightLoadingStation}) {
                drivetrain.positionAndButtUp(motionProfile, rightLoadingStation, 0.5.Inch, 10.Degree)
            }
            runWhile({goToLeftCloseCargo}) {
                drivetrain.positionAndButtUp(motionProfile, leftCloseCargo, 0.5.Inch, 10.Degree)

            }
            runWhile({goToRightCloseCargo}) {
                drivetrain.positionAndButtUp(motionProfile, rightCloseCargo, 0.5.Inch, 10.Degree)
            }

            runWhile({goToLeftMiddleCargo}) {
                drivetrain.positionAndButtUp(motionProfile, leftMiddleCargo, 0.5.Inch, 10.Degree)

            }
            runWhile({goToRightMiddleCargo}) {
                drivetrain.positionAndButtUp(motionProfile, rightMiddleCargo, 0.5.Inch, 10.Degree)
            }

            runWhile({goToLeftFarCargo}) {
                drivetrain.positionAndButtUp(motionProfile, leftFarCargo, 0.5.Inch, 10.Degree)

            }
            runWhile({goToRightFarCargo}) {
                drivetrain.positionAndButtUp(motionProfile, rightFarCargo, 0.5.Inch, 10.Degree)
            }

            runWhile({goToLeftCloseRocket}) {
                drivetrain.positionAndButtUp(motionProfile, leftCloseRocket, 0.5.Inch, 10.Degree)
            }
            runWhile({goToRightCloseRocket}) {
                drivetrain.positionAndButtUp(motionProfile, rightCloseRocket, 0.5.Inch, 10.Degree)
            }
            runWhile({goToLeftFarRocket}) {
                drivetrain.positionAndButtUp(motionProfile, leftFarRocket, 0.5.Inch, 10.Degree)
            }
            runWhile({goToRightFarRocket}) {
                drivetrain.positionAndButtUp(motionProfile, rightFarRocket, 0.5.Inch, 10.Degree)
            }
            runWhile({zeroAtLeftLoadingStation}) {
                zero(leftLoadingStation)
            }
            runWhile({zeroAtRightLoadingStation}) {
                zero(rightLoadingStation)
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

suspend fun DrivetrainComponent.positionAndButtUp(motionProfile: (Length) -> Velocity, targetPos: Position, posTolerance: Length, turnTolerance: Angle) = startChoreo("Go to position and butt up") {

    val position by hardware.position.readEagerly().withoutStamps

    choreography {
        waypoint(motionProfile, targetPos.vector, posTolerance)
        turn(targetPos.bearing `coterminal -` position.bearing, turnTolerance)
        openLoop(50.Percent)
    }
}
