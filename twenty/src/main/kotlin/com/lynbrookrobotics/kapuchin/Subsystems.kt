package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.turret.*
import com.lynbrookrobotics.kapuchin.subsystems.storage.*
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.RobotController
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.*
import java.io.File
import kotlin.system.exitProcess

class Subsystems(val drivetrain: DrivetrainComponent,
                 val electrical: ElectricalSystemHardware,
                 val driver: DriverHardware,
                 val operator: OperatorHardware,
                 val rumble: RumbleComponent,

                 val climberPivot: ClimberPivotComponent?,
                 val climberWinch: ClimberWinchComponent?,
                 val controlPanelPivot: ControlPanelPivotComponent?,
                 val controlPanelSpinner: ControlPanelSpinnerComponent?,
                 val intakeRollers: IntakeRollersComponent?,
                 val intakeSlider: IntakeSliderComponent?,
                 val limelight: LimelightComponent?,
                 val flywheel: FlywheelComponent?,
                 val turret: TurretComponent?,
                 val feederRoller: FeederRollerComponent?
                 val shooterHood: ShooterHoodComponent?,
                 val carousel: CarouselComponent?
) : Named by Named("Subsystems") {

    suspend fun teleop() {
        HAL.observeUserProgramTeleop()
        runAll(
                { climberTeleop() },
                { controlPanelTeleop() },
                { drivetrain.teleop(driver) },
                { intakeTeleop() },
                { limelight?.autoZoom() },
                { shooterTeleop() }
        )
    }

    suspend fun warmup() {
        runAll(
                { drivetrain.teleop(driver) },
                { limelight?.autoZoom() },
                { shooterTeleop() },
                {
                    while (isActive) {
                        delay(0.3.Second)
                        if (RobotController.getUserButton()) exitProcess(0)
                    }
                }
        )
    }

    companion object : Named by Named("Subsystems") {

        private val isCorrupted by pref(true)

        init {
            if (isCorrupted) {
                log(Error) { "The config seems to be corrupted. Attempting restoration." }
                NetworkTableInstance.getDefault().stopServer()

                val ntPath = "/home/lvuser/networktables.ini"

                Thread.currentThread()
                        .contextClassLoader
                        .getResourceAsStream("com/lynbrookrobotics/kapuchin/configbackups/networktables.ini")!!
                        .copyTo(File(ntPath).outputStream())
                File("$ntPath.bak").delete()

                exitProcess(1)
            }
        }

        private val initClimberPivot by pref(true)
        private val initClimberWinch by pref(true)
        private val initControlPanelPivot by pref(true)
        private val initControlPanelSpinner by pref(true)
        private val initIntakeRollers by pref(true)
        private val initIntakeSlider by pref(true)
        private val initLimelight by pref(true)
        private val initFlywheel by pref(true)
        private val initTurret by pref(true)
        private val initFeederRoller by pref(true)
        private val initShooterHood by pref(true)
        private val initCarousel by pref(true)

        var instance: Subsystems? = null
            private set

        val pneumaticTicker = ticker(Low, 50.milli(Second), "Pneumatic System Ticker")
        val shooterTicker = ticker(Highest, 30.milli(Second), "Shooter System Ticker")
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")
        fun sharedTickerTiming(): Nothing = error("Subsystem should use shared ticker values!")

        fun concurrentInit() = scope.launch {
            supervisorScope {
                suspend fun <T> t(producer: suspend () -> T): T? = try {
                    producer()
                } catch (t: Throwable) {
                    if (crashOnFailure) throw t else null
                }

                suspend fun <R> i(shouldInit: Boolean, producer: suspend () -> R) = async { if (shouldInit) producer() else null }

                val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
                val electricalAsync = async { ElectricalSystemHardware() }
                val driverAsync = async { DriverHardware() }
                val operatorAsync = async { OperatorHardware() }
                val rumbleAsync = async { RumbleComponent(RumbleHardware(driverAsync.await(), operatorAsync.await())) }

                val climberPivotAsync = i(initClimberPivot) { ClimberPivotComponent(ClimberPivotHardware()) }
                val climberWinchAsync = i(initClimberWinch) { ClimberWinchComponent(ClimberWinchHardware()) }
                val controlPanelPivotAsync = i(initControlPanelPivot) { ControlPanelPivotComponent(ControlPanelPivotHardware()) }
                val controlPanelSpinnerAsync = i(initControlPanelSpinner) { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware(driverAsync.await())) }
                val intakeRollersAsync = i(initIntakeRollers) { IntakeRollersComponent(IntakeRollersHardware()) }
                val intakeSliderAsync = i(initIntakeSlider) { IntakeSliderComponent(IntakeSliderHardware()) }
                val limelightAsync = i(initLimelight) { LimelightComponent(LimelightHardware()) }
                val flywheelAsync = i(initFlywheel) { FlywheelComponent(FlywheelHardware()) }
                val turretAsync = i(initTurret) { TurretComponent(TurretHardware()) }
                val feederRollerAsync = i(initFeederRoller) { FeederRollerComponent(FeederRollerHardware()) }
                val shooterHoodAsync = i(initShooterHood) { ShooterHoodComponent(ShooterHoodHardware()) }
                val carouselAsync = i(initCarousel) { CarouselComponent(CarouselHardware()) }

                instance = Subsystems(
                        drivetrainAsync.await(),
                        electricalAsync.await(),
                        driverAsync.await(),
                        operatorAsync.await(),
                        rumbleAsync.await(),

                        t { climberPivotAsync.await() },
                        t { climberWinchAsync.await() },
                        t { controlPanelPivotAsync.await() },
                        t { controlPanelSpinnerAsync.await() },
                        t { intakeRollersAsync.await() },
                        t { intakeSliderAsync.await() },
                        t { limelightAsync.await() },
                        t { flywheelAsync.await() },
                        t { turretAsync.await() },
                        t { feederRollerAsync.await() },
                        t { shooterHoodAsync.await() },
                        t { carouselAsync.await() }
                )
            }
        }.also { runBlocking { it.join() } }

        fun sequentialInit() {
            fun <T> t(f: () -> T): T? = try {
                f()
            } catch (t: Throwable) {
                if (crashOnFailure) throw t else null
            }

            fun <R> i(shouldInit: Boolean, producer: () -> R) = if (shouldInit) producer() else null

            val driver = DriverHardware()
            val operator = OperatorHardware()
            instance = Subsystems(
                    DrivetrainComponent(DrivetrainHardware()),
                    ElectricalSystemHardware(),
                    driver,
                    operator,
                    RumbleComponent(RumbleHardware(driver, operator)),

                    i(initClimberPivot) { t { ClimberPivotComponent(ClimberPivotHardware()) } },
                    i(initClimberWinch) { t { ClimberWinchComponent(ClimberWinchHardware()) } },
                    i(initControlPanelPivot) { t { ControlPanelPivotComponent(ControlPanelPivotHardware()) } },
                    i(initControlPanelSpinner) { t { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware(driver)) } },
                    i(initIntakeRollers) { t { IntakeRollersComponent(IntakeRollersHardware()) } },
                    i(initIntakeSlider) { t { IntakeSliderComponent(IntakeSliderHardware()) } },
                    i(initLimelight) { t { LimelightComponent(LimelightHardware()) } },
                    i(initFlywheel) { t { FlywheelComponent(FlywheelHardware()) } },
                    i(initTurret) { t { TurretComponent(TurretHardware()) } },
                    i(initFeederRoller) { t { FeederRollerComponent(FeederRollerHardware()) } },
                    i(initShooterHood) { t { ShooterHoodComponent(ShooterHoodHardware()) } },
                    i(initCarousel) { t { CarouselComponent(CarouselHardware()) } }
            )
        }
    }
}