package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.routines.*
import com.lynbrookrobotics.kapuchin.subsystems.*
import com.lynbrookrobotics.kapuchin.subsystems.driver.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.hookslider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.collector.slider.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.*
import com.lynbrookrobotics.kapuchin.subsystems.intake.handoff.pivot.*
import com.lynbrookrobotics.kapuchin.subsystems.lift.*
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

object Subsystems : Named by Named("subsystems") {

    val pneumaticTicker = ticker(Low, 100.milli(Second), "Pneumatic System Ticker")
    val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")

    lateinit var drivetrain: DrivetrainComponent private set

    lateinit var teleop: TeleopComponent private set
    lateinit var driver: DriverHardware private set
    lateinit var operator: OperatorHardware private set
    lateinit var leds: LedHardware private set

    lateinit var lineScanner: LineScannerHardware private set
    lateinit var collectorPivot: CollectorPivotComponent private set
    lateinit var collectorRollers: CollectorRollersComponent private set
    lateinit var collectorSlider: CollectorSliderComponent private set
    lateinit var hook: HookComponent private set
    lateinit var hookSlider: HookSliderComponent private set
    lateinit var handoffPivot: HandoffPivotComponent private set
    lateinit var handoffRollers: HandoffRollersComponent private set
    lateinit var panelEjector: HatchPanelEjectorComponent private set
    lateinit var lift: LiftComponent private set
    lateinit var climber: ClimberComponent private set
    lateinit var limelight: LimelightHardware private set
    lateinit var electrical: ElectricalSystemHardware private set

    fun concurrentInit() = runBlocking {
        val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }

        val driverAsync = async { DriverHardware() }
        val operatorAsync = async { OperatorHardware() }
        val ledsAsync = async { LedHardware() }
        val teleopAsync = async {
            TeleopComponent(TeleopHardware(
                    driverAsync.await(), operatorAsync.await(), ledsAsync.await()
            ))
        }

        val lineScannerAsync = async { LineScannerHardware() }
        val collectorPivotAsync = async { CollectorPivotComponent(CollectorPivotHardware()) }
        val collectorRollersAsync = async { CollectorRollersComponent(CollectorRollersHardware()) }
        val collectorSliderAsync = async { CollectorSliderComponent(CollectorSliderHardware()) }
        val hookAsync = async { HookComponent(HookHardware()) }
        val hookSliderAsync = async { HookSliderComponent(HookSliderHardware()) }
        val handoffPivotAsync = async { HandoffPivotComponent(HandoffPivotHardware()) }
        val handoffRollersAsync = async { HandoffRollersComponent(HandoffRollersHardware()) }
        val panelEjectorAsync = async { HatchPanelEjectorComponent(HatchPanelEjectorHardware()) }
        val liftAsync = async { LiftComponent(LiftHardware()) }
        val climberAsync = async { ClimberComponent(ClimberHardware()) }
        val limelightAsync = async { LimelightHardware() }
        val electricalAsync = async { ElectricalSystemHardware() }

        suspend fun t(f: suspend () -> Unit) = try {
            f()
        } catch (t: Throwable) {
            if (crashOnFailure) throw t else Unit
        }

        t { drivetrain = drivetrainAsync.await() }

        t { teleop = teleopAsync.await() }
        t { driver = driverAsync.await() }
        t { operator = operatorAsync.await() }
        t { leds = ledsAsync.await() }

        t { lineScanner = lineScannerAsync.await() }
        t { collectorPivot = collectorPivotAsync.await() }
        t { collectorRollers = collectorRollersAsync.await() }
        t { collectorSlider = collectorSliderAsync.await() }
        t { hook = hookAsync.await() }
        t { hookSlider = hookSliderAsync.await() }
        t { handoffPivot = handoffPivotAsync.await() }
        t { handoffRollers = handoffRollersAsync.await() }
        t { panelEjector = panelEjectorAsync.await() }
        t { lift = liftAsync.await() }
        t { climber = climberAsync.await() }
        t { limelight = limelightAsync.await() }
        t { electrical = electricalAsync.await() }
    }

    fun sequentialInit() {
        fun t(f: () -> Unit) = try {
            f()
        } catch (t: Throwable) {
            if (crashOnFailure) throw t else Unit
        }

        t { drivetrain = DrivetrainComponent(DrivetrainHardware()) }

        t { driver = DriverHardware() }
        t { operator = OperatorHardware() }
        t { leds = LedHardware() }
        t { teleop = TeleopComponent(TeleopHardware(driver, operator, leds)) }

        t { lineScanner = LineScannerHardware() }
        t { collectorPivot = CollectorPivotComponent(CollectorPivotHardware()) }
        t { collectorRollers = CollectorRollersComponent(CollectorRollersHardware()) }
        t { collectorSlider = CollectorSliderComponent(CollectorSliderHardware()) }
        t { hook = HookComponent(HookHardware()) }
        t { hookSlider = HookSliderComponent(HookSliderHardware()) }
        t { handoffPivot = HandoffPivotComponent(HandoffPivotHardware()) }
        t { handoffRollers = HandoffRollersComponent(HandoffRollersHardware()) }
        t { panelEjector = HatchPanelEjectorComponent(HatchPanelEjectorHardware()) }
        t { lift = LiftComponent(LiftHardware()) }
        t { climber = ClimberComponent(ClimberHardware()) }
        t { limelight = LimelightHardware() }
        t { electrical = ElectricalSystemHardware() }
    }

    suspend fun teleop() {
        runAll(
                { drivetrainTeleop(drivetrain, driver, limelight) },
                {
                    HAL.observeUserProgramTeleop()
                    System.gc()
                }
        )
    }

    suspend fun warmup() {
        runAll(
                { drivetrain.warmup() }
        )
    }

    suspend fun followWaypoints() {
        drivetrain.waypoint({ 3.FootPerSecond }, UomVector(0.Foot, 5.Foot), 2.Inch)
        delay(1.Second)
        drivetrain.waypoint({ 3.FootPerSecond }, UomVector(5.Foot, 5.Foot), 2.Inch)
        delay(1.Second)
        drivetrain.waypoint({ 3.FootPerSecond }, UomVector(5.Foot, 0.Foot), 2.Inch)
        delay(1.Second)
        drivetrain.waypoint({ 3.FootPerSecond }, UomVector(0.Foot, 0.Foot), 2.Inch)
        delay(1.Second)
    }

    suspend fun llAlign() {
        llAlign(drivetrain, limelight)
    }

    suspend fun backAndForthAuto() {
        //        while (true) {
//            withTimeout(1.Second) {
//                drivetrain.driveStraight(
//                        8.Foot, 0.Degree,
//                        1.Inch, 2.Degree,
//                        2.FootPerSecondSquared,
//                        3.FootPerSecond
//                )
//            }
//
//            delay(1.Second)
//
//            withTimeout(1.Second) {
//                drivetrain.driveStraight(
//                        -8.Foot, 0.Degree,
//                        1.Inch, 2.Degree,
//                        2.FootPerSecondSquared,
//                        3.FootPerSecond
//                )
//            }
//
//            delay(1.Second)
//        }
    }
}