package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.analog.adis16448.frc.ADIS16448_IMU
import com.lynbrookrobotics.kapuchin.control.conversion.EncoderConversion
import com.lynbrookrobotics.kapuchin.control.conversion.GearTrain
import com.lynbrookrobotics.kapuchin.control.data.UomVector
import com.lynbrookrobotics.kapuchin.control.data.stampWith
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.hardw
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.with
import com.lynbrookrobotics.kapuchin.hardware.TimeStampedEncoder
import com.lynbrookrobotics.kapuchin.logging.Grapher.Companion.graph
import com.lynbrookrobotics.kapuchin.preferences.pref
import com.lynbrookrobotics.kapuchin.subsystems.SubsystemHardware
import com.lynbrookrobotics.kapuchin.timing.Priority
import com.lynbrookrobotics.kapuchin.timing.clock.EventLoop
import edu.wpi.first.wpilibj.Counter
import edu.wpi.first.wpilibj.DigitalOutput
import edu.wpi.first.wpilibj.Spark
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.`±`
import info.kunalsheth.units.math.milli

class DrivetrainHardware : SubsystemHardware<DrivetrainHardware, DrivetrainComponent>() {
    override val priority = Priority.RealTime
    override val period = 15.milli(Second)
    override val syncThreshold = 3.milli(Second)
    override val name = "Drivetrain"

    private val leftEscPort by pref(2)
    private val rightEscPort by pref(3)

    private val jitterPulsePinNumber by pref(8)
    private val jitterReadPinNumber by pref(9)
    val jitterPulsePin by hardw { DigitalOutput(jitterPulsePinNumber) }
    val jitterReadPin by hardw { Counter(jitterReadPinNumber) }

    val leftEsc by hardw { Spark(leftEscPort) }
    val rightEsc by hardw { Spark(rightEscPort) }
            .configure { it.inverted = true }

    private val wheelRadius by pref(3, Inch)

    private val encoderConversion by pref {
        val encoderGear by pref(18)
        val wheelGear by pref(74)
        val resolution by pref(4096)

        ({
            EncoderConversion(
                    resolution,
                    GearTrain(encoderGear, wheelGear).inputToOutput(1.Turn)
            )
        })
    }

    private val leftEncoderA by pref(0)
    private val leftEncoderB by pref(1)
    private val rightEncoderA by pref(2)
    private val rightEncoderB by pref(3)

    private val leftEncoder by hardw { TimeStampedEncoder(leftEncoderA, leftEncoderB) }
            .verify("the robot should not be moved during startup") { it.stopped }
    private val rightEncoder by hardw { TimeStampedEncoder(rightEncoderA, rightEncoderB) }
            .verify("the robot should not be moved during startup") { it.stopped }

    val leftPosition = sensor(leftEncoder) {
        wheelRadius * encoderConversion.angle(ticks) / Radian stampWith timeStamp
    }.with(graph("Left Position", Foot))

    val rightPosition = sensor(rightEncoder) {
        wheelRadius * encoderConversion.angle(ticks) / Radian stampWith timeStamp
    }.with(graph("Right Position", Foot))

    val leftSpeed = sensor(leftEncoder) {
        wheelRadius * encoderConversion.angle(rate) / Radian stampWith timeStamp
    }.with(graph("Left Speed", FootPerSecond))

    val rightSpeed = sensor(rightEncoder) {
        wheelRadius * encoderConversion.angle(rate) / Radian stampWith timeStamp
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
            setOf(gyroInput, leftPosition, rightPosition, leftSpeed, rightSpeed).forEach {
                it.optimizedRead(time, syncThreshold)
            }
        }
    }
}
