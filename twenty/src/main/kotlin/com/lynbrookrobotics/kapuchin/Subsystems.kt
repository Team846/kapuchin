package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.logging.Level.*
import com.lynbrookrobotics.kapuchin.preferences.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.carousel.*
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.controlpanel.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.*
import com.lynbrookrobotics.kapuchin.subsystems.limelight.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.flywheel.*
import com.lynbrookrobotics.kapuchin.subsystems.shooter.turret.*
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
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.system.exitProcess

class Subsystems(val drivetrain: DrivetrainComponent,
                 val carousel: CarouselComponent,
                 val electrical: ElectricalSystemHardware,
                 val limelight: LimelightComponent,

                 val driver: DriverHardware,
                 val operator: OperatorHardware,
                 val rumble: RumbleComponent,

                 val climberPivot: ClimberPivotComponent?,
                 val climberWinch: ClimberWinchComponent?,
                 val controlPanelPivot: ControlPanelPivotComponent?,
                 val controlPanelSpinner: ControlPanelSpinnerComponent?,
                 val intakeRollers: IntakeRollersComponent?,
                 val intakeSlider: IntakeSliderComponent?,
                 val flywheel: FlywheelComponent?,
                 val turret: TurretComponent?,
                 val feederRoller: FeederRollerComponent?,
                 val flashlight: FlashlightComponent?,
                 val shooterHood: ShooterHoodComponent?
) : Named by Named("Subsystems") {

    suspend fun teleop() {
        HAL.observeUserProgramTeleop()
        runAll(
                { climberTeleop() },
                { controlPanelTeleop() },
                { digestionTeleop() },
                {
                    launchWhenever(
                            { limelight.routine == null } to choreography { limelight.autoZoom() },
                            { drivetrain.routine == null } to choreography { drivetrain.teleop(driver) }
                    )
                }
        )
    }

    suspend fun warmup() {
        runAll(
                { drivetrain.teleop(driver) },
                { limelight.autoZoom() },
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

        private val initClimberPivot by pref(false)
        private val initClimberWinch by pref(false)
        private val initControlPanelPivot by pref(false)
        private val initControlPanelSpinner by pref(false)
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
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")

        val SubsystemHardware<*, *>.sharedTickerTiming
            get() = object : ReadOnlyProperty<SubsystemHardware<*, *>, Time> {
                override fun getValue(thisRef: SubsystemHardware<*, *>, property: KProperty<*>): Time {
                    thisRef.log(Error) { "Subsystem should use shared ticker values!" }
                    return 20.milli(Second)
                }
            }

        fun concurrentInit() = scope.launch {
            supervisorScope {
                suspend fun <T> t(producer: suspend () -> T): T? = try {
                    producer()
                } catch (t: Throwable) {
                    if (crashOnFailure) throw t else null
                }

                suspend fun <R> i(shouldInit: Boolean, producer: suspend () -> R) = async { if (shouldInit) producer() else null }

                val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
                val carouselAsync = async { CarouselComponent(CarouselHardware()) }
                val electricalAsync = async { ElectricalSystemHardware() }
                val limelightAsync = async { LimelightComponent(LimelightHardware()) }

                val driverAsync = async { DriverHardware() }
                val operatorAsync = async { OperatorHardware() }
                val rumbleAsync = async { RumbleComponent(RumbleHardware(driverAsync.await(), operatorAsync.await())) }

                val climberPivotAsync = i(initClimberPivot) { ClimberPivotComponent(ClimberPivotHardware()) }
                val climberWinchAsync = i(initClimberWinch) { ClimberWinchComponent(ClimberWinchHardware()) }
                val controlPanelPivotAsync = i(initControlPanelPivot) { ControlPanelPivotComponent(ControlPanelPivotHardware()) }
                val controlPanelSpinnerAsync = i(initControlPanelSpinner) { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware(driverAsync.await())) }
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

                        t { climberPivotAsync.await() },
                        t { climberWinchAsync.await() },
                        t { controlPanelPivotAsync.await() },
                        t { controlPanelSpinnerAsync.await() },
                        t { intakeRollersAsync.await() },
                        t { intakeSliderAsync.await() },
                        t { flywheelAsync.await() },
                        t { turretAsync.await() },
                        t { feederRollerAsync.await() },
                        t { flashlightAsync.await() },
                        t { shooterHoodAsync.await() }
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
                    CarouselComponent(CarouselHardware()),
                    ElectricalSystemHardware(),
                    LimelightComponent(LimelightHardware()),

                    driver,
                    operator,
                    RumbleComponent(RumbleHardware(driver, operator)),

                    i(initClimberPivot) { t { ClimberPivotComponent(ClimberPivotHardware()) } },
                    i(initClimberWinch) { t { ClimberWinchComponent(ClimberWinchHardware()) } },
                    i(initControlPanelPivot) { t { ControlPanelPivotComponent(ControlPanelPivotHardware()) } },
                    i(initControlPanelSpinner) { t { ControlPanelSpinnerComponent(ControlPanelSpinnerHardware(driver)) } },
                    i(initIntakeRollers) { t { IntakeRollersComponent(IntakeRollersHardware()) } },
                    i(initIntakeSlider) { t { IntakeSliderComponent(IntakeSliderHardware()) } },
                    i(initFlywheel) { t { FlywheelComponent(FlywheelHardware()) } },
                    i(initTurret) { t { TurretComponent(TurretHardware()) } },
                    i(initFeederRoller) { t { FeederRollerComponent(FeederRollerHardware()) } },
                    i(initFlashlight) { t { FlashlightComponent(FlashlightHardware()) } },
                    i(initShooterHood) { t { ShooterHoodComponent(ShooterHoodHardware()) } }
            )
        }
    }
}