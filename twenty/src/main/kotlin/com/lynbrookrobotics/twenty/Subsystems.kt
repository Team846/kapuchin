package com.lynbrookrobotics.twenty

import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import com.lynbrookrobotics.twenty.choreos.*
import com.lynbrookrobotics.twenty.choreos.auto.*
import com.lynbrookrobotics.twenty.routines.*
import com.lynbrookrobotics.twenty.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselComponent
import com.lynbrookrobotics.twenty.subsystems.carousel.CarouselHardware
import com.lynbrookrobotics.twenty.subsystems.climber.*
import com.lynbrookrobotics.twenty.subsystems.driver.*
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.twenty.subsystems.drivetrain.DrivetrainHardware
import com.lynbrookrobotics.twenty.subsystems.intake.*
import com.lynbrookrobotics.twenty.subsystems.limelight.LimelightComponent
import com.lynbrookrobotics.twenty.subsystems.limelight.LimelightHardware
import com.lynbrookrobotics.twenty.subsystems.shooter.*
import com.lynbrookrobotics.twenty.subsystems.shooter.flywheel.FlywheelComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.flywheel.FlywheelHardware
import com.lynbrookrobotics.twenty.subsystems.shooter.turret.TurretComponent
import com.lynbrookrobotics.twenty.subsystems.shooter.turret.TurretHardware
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.system.exitProcess

class Subsystems(
    val drivetrain: DrivetrainComponent,
    val carousel: CarouselComponent,
    val electrical: ElectricalSystemHardware,
    val limelight: LimelightComponent,

    val driver: DriverHardware,
    val operator: OperatorHardware,
    val rumble: RumbleComponent,
    val leds: LedComponent?,

    val climberPivot: ClimberPivotComponent?,
    val climberWinch: ClimberWinchComponent?,

    val intakeRollers: IntakeRollersComponent?,
    val intakeSlider: IntakeSliderComponent?,
    val flywheel: FlywheelComponent?,
    val turret: TurretComponent?,
    val feederRoller: FeederRollerComponent?,
    val flashlight: FlashlightComponent?,
    val shooterHood: ShooterHoodComponent?,
) : Named by Named("Subsystems") {

    private val autoId by pref(0)
    val journalId by pref(0)
    val journalReverse by pref(false)

    var initialBearing = 0.Degree

    suspend fun auto() = coroutineScope {
        val autos = listOf(
            ::autoGetOffLine,
            ::auto3BallReverse,
            ::auto3BallForward,
            { auto6BallStraight(initialBearing, currentTime) },
            { auto6BallCurved(initialBearing, currentTime) },
            { auto5Ball(initialBearing, currentTime) },
        )

        launch { turret?.trackTarget(drivetrain, limelight) }
        delay(AutoPrefs.initialDelay)

        when (autoId) {
            !in autos.indices -> {
                log(Error) { "$autoId isn't an auto!! you fucked up!!!" }
                autoGetOffLine()
            }
            else -> autos[autoId].invoke()
        }
    }

    suspend fun teleop() = runAll(
        { climberTeleop() },
        { digestionTeleop() },
        {
            launchWhenever(
                { limelight.routine == null } to { limelight.autoZoom() },
                { drivetrain.routine == null } to { drivetrain.teleop(driver) }
            )
        }
    )


    suspend fun test() = runAll(
        { drivetrain.teleop(driver) },
//        { climberTest() },
//        { digestionTest() },
        { journalPath() }
    )

    suspend fun warmup() = runAll(
        { drivetrain.teleop(driver) },
        { limelight.autoZoom() },
        {
            while (isActive) {
                delay(0.3.Second)
                if (RobotController.getUserButton()) exitProcess(0)
            }
        },
        {
            while (isActive) {
                if (turret?.hardware?.atZero?.optimizedRead(currentTime, 0.Second)?.y == true) {
                    initialBearing = drivetrain.hardware.position.optimizedRead(currentTime, 0.Second).y.bearing
                    log(Debug) { "Set initial bearing to ${initialBearing.Degree} deg" }
                }
            }
        }
    )

    companion object : Named by Named("Subsystems") {

        private val isCorrupted by pref(true)

        init {
            if (isCorrupted) {
                log(Error) { "The config seems to be corrupted. Attempting restoration." }
                NetworkTableInstance.getDefault().stopServer()

                val ntPath = "/home/lvuser/networktables.ini"

                Thread.currentThread()
                    .contextClassLoader
                    .getResourceAsStream("com/lynbrookrobotics/twenty/configbackups/networktables.ini")!!
                    .copyTo(File(ntPath).outputStream())
                File("$ntPath.bak").delete()

                exitProcess(1)
            }
        }

        private val initLeds by pref(false)

        private val initClimberPivot by pref(false)
        private val initClimberWinch by pref(false)

        private val initIntakeRollers by pref(false)
        private val initIntakeSlider by pref(false)
        private val initFlywheel by pref(false)
        private val initTurret by pref(false)
        private val initFeederRoller by pref(false)
        private val initFlashlight by pref(false)
        private val initShooterHood by pref(false)

        var instance: Subsystems? = null
            private set

        val pneumaticTicker = ticker(Low, 50.milli(Second), "Pneumatic System Ticker")
        val shooterTicker = ticker(Highest, 30.milli(Second), "Shooter System Ticker")
        val uiTicker = ticker(Lowest, 500.milli(Second), "UI Ticker")

        fun concurrentInit() = scope.launch {
            supervisorScope {
                suspend fun <T> t(producer: suspend () -> T): T? = try {
                    producer()
                } catch (t: Throwable) {
                    if (crashOnFailure) throw t else null
                }

                @Suppress("DeferredIsResult")
                suspend fun <R> i(shouldInit: Boolean, producer: suspend () -> R) =
                    async { if (shouldInit) producer() else null }

                val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
                val carouselAsync = async { CarouselComponent(CarouselHardware()) }
                val electricalAsync = async { ElectricalSystemHardware() }
                val limelightAsync = async { LimelightComponent(LimelightHardware()) }

                val driverAsync = async { DriverHardware() }
                val operatorAsync = async { OperatorHardware() }
                val rumbleAsync = async { RumbleComponent(RumbleHardware(operatorAsync.await())) }
                val ledsAsync = i(initLeds) { LedComponent(LedHardware()) }

                val climberPivotAsync = i(initClimberPivot) { ClimberPivotComponent(ClimberPivotHardware()) }
                val climberWinchAsync = i(initClimberWinch) { ClimberWinchComponent(ClimberWinchHardware()) }

                val intakeRollersAsync = i(initIntakeRollers) { IntakeRollersComponent(IntakeRollersHardware()) }
                val intakeSliderAsync = i(initIntakeSlider) { IntakeSliderComponent(IntakeSliderHardware()) }
                val flywheelAsync = i(initFlywheel) { FlywheelComponent(FlywheelHardware()) }
                val turretAsync = i(initTurret) { TurretComponent(TurretHardware()) }
                val feederRollerAsync = i(initFeederRoller) { FeederRollerComponent(FeederRollerHardware()) }
                val flashlightAsync = i(initFlashlight) { FlashlightComponent(FlashlightHardware()) }
                val shooterHoodAsync = i(initShooterHood) { ShooterHoodComponent(ShooterHoodHardware()) }

                instance = Subsystems(
                    drivetrainAsync.await(),
                    carouselAsync.await(),
                    electricalAsync.await(),
                    limelightAsync.await(),

                    driverAsync.await(),
                    operatorAsync.await(),
                    rumbleAsync.await(),
                    t { ledsAsync.await() },

                    t { climberPivotAsync.await() },
                    t { climberWinchAsync.await() },

                    t { intakeRollersAsync.await() },
                    t { intakeSliderAsync.await() },
                    t { flywheelAsync.await() },
                    t { turretAsync.await() },
                    t { feederRollerAsync.await() },
                    t { flashlightAsync.await() },
                    t { shooterHoodAsync.await() },
                )
            }
        }.also { runBlocking { it.join() } }
    }
}