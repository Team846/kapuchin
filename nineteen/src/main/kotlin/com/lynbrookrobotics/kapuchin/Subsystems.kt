package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.choreos.*
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.kinematics.*
import com.lynbrookrobotics.kapuchin.hardware.HardwareInit.Companion.crashOnFailure
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
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
import com.lynbrookrobotics.kapuchin.timing.*
import com.lynbrookrobotics.kapuchin.timing.scope
import com.lynbrookrobotics.kapuchin.timing.Priority.*
import com.lynbrookrobotics.kapuchin.timing.clock.*
import edu.wpi.first.hal.HAL
import info.kunalsheth.units.generated.*
import info.kunalsheth.units.math.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

class Subsystems(val drivetrain: DrivetrainComponent,
                 val electrical: ElectricalSystemHardware,

                 val teleop: TeleopComponent,
                 val driver: DriverHardware,
                 val operator: OperatorHardware,
                 val leds: LedHardware?,

                 val lineScanner: LineScannerHardware?,
                 val collectorPivot: CollectorPivotComponent?,
                 val collectorRollers: CollectorRollersComponent?,
                 val collectorSlider: CollectorSliderComponent?,
                 val hook: HookComponent?,
                 val hookSlider: HookSliderComponent?,
                 val handoffPivot: HandoffPivotComponent?,
                 val handoffRollers: HandoffRollersComponent?,
                 val velcroPivot: VelcroPivotComponent?,
                 val lift: LiftComponent?,
                 val climber: ClimberComponent?,
                 val limelight: LimelightHardware?
) : Named by Named("Subsystems") {

    suspend fun teleop() {
        System.gc()
        HAL.observeUserProgramTeleop()
        runAll(
                { drivetrainTeleop() },
                { intakeTeleop() },
                { liftTeleop() },
                { climberTeleop() }
        )
        System.gc()
    }

    suspend fun warmup() {
        System.gc()
        runAll(
                { drivetrain.warmup() }
        )
        System.gc()
    }

    val performance by pref(40, Percent)
    suspend fun followWaypoints() {
        val waypts = File("/tmp/journal.tsv").useLines { lns ->
            lns
                    .drop(1)
                    .map { it.split('\t') }
                    .map { it.map { tkn -> tkn.trim() } }
                    .map { Waypt(it[1].toDouble().Foot, it[2].toDouble().Foot) stampWith it[0].toDouble().Second }
                    .toList()
        }

        val traj = pathToTrajectory(
                waypts.map { (_, pt) -> pt },
                performance,
                drivetrain.maxSpeed,
                drivetrain.maxOmega
        )

        drivetrain.readJournal(2.Foot, 8.Inch, 5.FootPerSecondSquared, true, traj)

        freeze()
    }

    companion object : Named by Named("Subsystems") {

        val initLeds by pref(true)
        val initLineScanner by pref(true)
        val initCollectorPivot by pref(true)
        val initCollectorRollers by pref(true)
        val initCollectorSlider by pref(true)
        val initHook by pref(true)
        val initHookSlider by pref(true)
        val initHandoffPivot by pref(true)
        val initHandoffRollers by pref(true)
        val initVelcroPivot by pref(true)
        val initLift by pref(true)
        val initClimber by pref(true)
        val initLimelight by pref(true)

        var instance: Subsystems? = null
            private set

        val pneumaticTicker = ticker(Low, 100.milli(Second), "Pneumatic System Ticker")
        val uiBaselineTicker = ticker(Lowest, 500.milli(Second), "UI Baseline Ticker")

        fun concurrentInit() = runBlocking {
            val drivetrainAsync = async { DrivetrainComponent(DrivetrainHardware()) }
            val electricalAsync = async { ElectricalSystemHardware() }

            val driverAsync = async { DriverHardware() }
            val operatorAsync = async { OperatorHardware() }

            suspend fun <R> i(b: Boolean, f: suspend () -> R) = async { if (b) f() else null }

            val ledsAsync = i(initLeds) { LedHardware() }
            val lineScannerAsync = i(initLineScanner) { LineScannerHardware() }
            val collectorPivotAsync = i(initCollectorPivot) { CollectorPivotComponent(CollectorPivotHardware()) }
            val collectorRollersAsync = i(initCollectorRollers) { CollectorRollersComponent(CollectorRollersHardware()) }
            val collectorSliderAsync = i(initCollectorSlider) { CollectorSliderComponent(CollectorSliderHardware()) }
            val hookAsync = i(initHook) { HookComponent(HookHardware()) }
            val hookSliderAsync = i(initHookSlider) { HookSliderComponent(HookSliderHardware()) }
            val handoffPivotAsync = i(initHandoffPivot) { HandoffPivotComponent(HandoffPivotHardware()) }
            val handoffRollersAsync = i(initHandoffRollers) { HandoffRollersComponent(HandoffRollersHardware()) }
            val velcroPivotAsync = i(initVelcroPivot) { VelcroPivotComponent(VelcroPivotHardware()) }
            val liftAsync = i(initLift) { LiftComponent(LiftHardware()) }
            val climberAsync = i(initClimber) { ClimberComponent(ClimberHardware()) }
            val limelightAsync = i(initLimelight) { LimelightHardware() }

            suspend fun <T> t(f: suspend () -> T): T? = try {
                f()
            } catch (t: Throwable) {
                if (crashOnFailure) throw t else null
            }

            val teleopAsync = async {
                TeleopComponent(TeleopHardware(
                        t { driverAsync.await() }!!,
                        t { operatorAsync.await() }!!,
                        t { ledsAsync.await() }
                ))
            }

            instance = Subsystems(
                    t { drivetrainAsync.await() }!!,
                    t { electricalAsync.await() }!!,

                    t { teleopAsync.await() }!!,
                    t { driverAsync.await() }!!,
                    t { operatorAsync.await() }!!,
                    t { ledsAsync.await() },

                    t { lineScannerAsync.await() },
                    t { collectorPivotAsync.await() },
                    t { collectorRollersAsync.await() },
                    t { collectorSliderAsync.await() },
                    t { hookAsync.await() },
                    t { hookSliderAsync.await() },
                    t { handoffPivotAsync.await() },
                    t { handoffRollersAsync.await() },
                    t { velcroPivotAsync.await() },
                    t { liftAsync.await() },
                    t { climberAsync.await() },
                    t { limelightAsync.await() }
            )
        }

        fun sequentialInit() {
            fun <T> t(f: () -> T): T? = try {
                f()
            } catch (t: Throwable) {
                if (crashOnFailure) throw t else null
            }

            fun <R> i(b: Boolean, f: () -> R) = if (b) f() else null

            val driver = t { DriverHardware() }!!
            val operator = t { OperatorHardware() }!!
            val leds = i(initLeds) { t { LedHardware() } }
            val teleop = t { TeleopComponent(TeleopHardware(driver, operator, leds)) }!!

            instance = Subsystems(
                    t { DrivetrainComponent(DrivetrainHardware()) }!!,
                    t { ElectricalSystemHardware() }!!,
                    teleop,
                    driver,
                    operator,
                    leds,
                    i(initLineScanner) { t { LineScannerHardware() } },
                    i(initCollectorPivot) { t { CollectorPivotComponent(CollectorPivotHardware()) } },
                    i(initCollectorRollers) { t { CollectorRollersComponent(CollectorRollersHardware()) } },
                    i(initCollectorSlider) { t { CollectorSliderComponent(CollectorSliderHardware()) } },
                    i(initHook) { t { HookComponent(HookHardware()) } },
                    i(initHookSlider) { t { HookSliderComponent(HookSliderHardware()) } },
                    i(initHandoffPivot) { t { HandoffPivotComponent(HandoffPivotHardware()) } },
                    i(initHandoffRollers) { t { HandoffRollersComponent(HandoffRollersHardware()) } },
                    i(initVelcroPivot) { t { VelcroPivotComponent(VelcroPivotHardware()) } },
                    i(initLift) { t { LiftComponent(LiftHardware()) } },
                    i(initClimber) { t { ClimberComponent(ClimberHardware()) } },
                    i(initLimelight) { t { LimelightHardware() } }
            )
        }
    }
}