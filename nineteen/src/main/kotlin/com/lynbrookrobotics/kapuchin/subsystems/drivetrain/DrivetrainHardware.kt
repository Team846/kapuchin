package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.analog.adis16448.frc.ADIS16448_IMU
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.lynbrookrobotics.kapuchin.control.conversion.EncoderConversion
import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.data.Position
import com.lynbrookrobotics.kapuchin.control.data.TwoSided
import com.lynbrookrobotics.kapuchin.control.data.UomVector
import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.control.math.simpleVectorTracking
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.with
import com.lynbrookrobotics.kapuchin.hardware.TicksToSerial
import com.lynbrookrobotics.kapuchin.hardware.configMaster
import com.lynbrookrobotics.kapuchin.hardware.configSlave
import com.lynbrookrobotics.kapuchin.hardware.lazyOutput
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import com.lynbrookrobotics.kapuchin.timing.currentTime
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.SerialPort
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.`±`
import info.kunalsheth.units.math.milli

class DrivetrainHardware : SubsystemHardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 15.milli(Second)
    override val syncThreshold = 3.milli(Second)
    override val name = "Drivetrain"


    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)
    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }


    private val leftMasterEscId by pref(10)
    private val leftSlaveEscId by pref(11)
    private val rightMasterEscId by pref(12)
    private val rightSlaveEscId by pref(13)

    val leftEscInversion by pref(false)
    val rightEscInversion by pref(true)

    val leftMasterEsc by hardw { TalonSRX(leftMasterEscId) }.configure {
        configMaster(it, 12.Volt, 30.Ampere, FeedbackDevice.QuadEncoder)
    }
    val leftSlaveEsc by hardw { VictorSPX(leftSlaveEscId) }.configure {
        configSlave(it, 12.Volt, 30.Ampere)
        it.follow(leftMasterEsc)
    }
    val leftLazyOutput = lazyOutput(leftMasterEsc)


    val rightMasterEsc by hardw { TalonSRX(rightMasterEscId) }.configure {
        configMaster(it, 12.Volt, 30.Ampere, FeedbackDevice.QuadEncoder)
        it.inverted = true
    }
    val rightSlaveEsc by hardw { VictorSPX(rightSlaveEscId) }.configure {
        configSlave(it, 12.Volt, 30.Ampere)
        it.follow(rightMasterEsc)
        it.inverted = true
    }
    val rightLazyOutput = lazyOutput(rightMasterEsc)

    private val wheelRadius by pref(3, Inch)

    private val encoderConversion by pref {
        val encoderGear by pref(18)
        val wheelGear by pref(74)
        val resolution by pref(1024)

        ({
            EncoderConversion(
                    resolution,
                    GearTrain(encoderGear, wheelGear).inputToOutput(1.Turn)
            )
        })
    }

    private val trackLength by pref(2.05, Foot)


    private val leftEncoderA by pref(0)
    private val rightEncoderA by pref(1)
    private val ticksToSerialPort by pref("kUSB1")

    private val leftEncoder by hardw { Counter(leftEncoderA) }
    private val rightEncoder by hardw { Counter(rightEncoderA) }
    private val ticksToSerial by hardw { TicksToSerial(SerialPort.Port.valueOf(ticksToSerialPort)) }

    data class Odometry(val left: Length, val right: Length, val xy: Position)

    private var leftMovingForward = false
    private var rightMovingForward = false
    private var leftPosition = 0.Foot
    private var rightPosition = 0.Foot
    private var xyPosition = Position(0.Foot, 0.Foot, 0.Degree)
    private val vectorTracking = simpleVectorTracking(trackLength, xyPosition)

    val leftTrim by pref(1.0018314)
    val rightTrim by pref(-1.00621994)

    val position = sensor {
        val startingLeftPosition = leftPosition
        val startingRightPosition = rightPosition

        ticksToSerial()
                .map { (l, r) ->
                    TwoSided(
                            wheelRadius * encoderConversion.angle(l.toDouble()) / Radian * leftTrim,
                            wheelRadius * encoderConversion.angle(r.toDouble()) / Radian * rightTrim
                    )
                }
                .forEach { (l, r) ->
                    xyPosition = vectorTracking(l, r)
                    leftPosition += l
                    rightPosition += r
                }

        leftMovingForward = leftPosition > startingLeftPosition
        rightMovingForward = rightPosition > startingRightPosition

        Odometry(leftPosition, rightPosition, xyPosition) stampWith currentTime
    }
            .with(graph("Left Position", Foot)) { it.left }
            .with(graph("Right Position", Foot)) { it.right }
            .with(graph("X Location", Foot)) { it.xy.x }
            .with(graph("Y Location", Foot)) { it.xy.y }
            .with(graph("Encoder Bearing", Degree)) { it.xy.bearing }


    private fun toSpeed(period: Time) =
            if (period == 0.Second) 0.FootPerSecond
            else wheelRadius * encoderConversion.angle(1.0) / Radian / period

    val leftSpeed = sensor {
        val speed = toSpeed(leftEncoder.period.Second)
        (if (leftMovingForward) speed else -speed) stampWith currentTime
    }.with(graph("Left Speed", FootPerSecond))

    val rightSpeed = sensor {
        val speed = toSpeed(rightEncoder.period.Second)
        (if (rightMovingForward) speed else -speed) stampWith currentTime
    }.with(graph("Right Speed", FootPerSecond))


    private val driftTolerance by pref(1, DegreePerSecond)
    private lateinit var startingAngle: Angle
    val imu by hardw { ADIS16448_IMU() }
            .configure { startingAngle = it.angle.Degree }
            .verify("Gyro should not drift after calibration") {
                it.rate.DegreePerSecond in 0.DegreePerSecond `±` driftTolerance
            }
    val gyroInput = sensor(imu) {
        GyroInput(angleZ.Degree - startingAngle, rate.DegreePerSecond, accelZ.DegreePerSecondSquared) stampWith it // lastSampleTime returns 0 ?
    }
            .with(graph("Bearing", Degree)) { it.angle }
            .with(graph("Angular Velocity", DegreePerSecond)) { it.velocity }

    val accelInput = sensor(imu) {
        UomVector(accelX.EarthGravity, accelY.EarthGravity, accelZ.EarthGravity) stampWith it // lastSampleTime returns 0 ?
    }
            .with(graph("Acceleration-X", FootPerSecondSquared)) { it.x }
            .with(graph("Acceleration-Y", FootPerSecondSquared)) { it.y }
            .with(graph("Acceleration-Z", FootPerSecondSquared)) { it.z }

    init {
        EventLoop.runOnTick { time ->
            setOf(gyroInput, position, leftSpeed, rightSpeed).forEach {
                it.optimizedRead(time, period)
            }
        }
    }
}
