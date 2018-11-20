package com.lynbrookrobotics.kapuchin

import com.lynbrookrobotics.kapuchin.logging.Named
import com.lynbrookrobotics.kapuchin.routines.Routine.Companion.launchAll
import com.lynbrookrobotics.kapuchin.routines.teleop.driveStraight
import com.lynbrookrobotics.kapuchin.routines.teleop.teleop
import com.lynbrookrobotics.kapuchin.subsystems.DriverHardware
import com.lynbrookrobotics.kapuchin.subsystems.ElectricalSystemHardware
import com.lynbrookrobotics.kapuchin.subsystems.LiftComponent
import com.lynbrookrobotics.kapuchin.subsystems.LiftHardware
import com.lynbrookrobotics.kapuchin.subsystems.climber.*
import com.lynbrookrobotics.kapuchin.subsystems.collector.*
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainComponent
import com.lynbrookrobotics.kapuchin.subsystems.drivetrain.DrivetrainHardware
import com.lynbrookrobotics.kapuchin.timing.scope
import edu.wpi.first.wpilibj.hal.HAL
import info.kunalsheth.units.generated.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class Subsystems(
        val forks: ForksComponent, val hooks: HooksComponent, val winch: WinchComponent,
        val clamp: ClampComponent, val pivot: PivotComponent, val rollers: RollersComponent,
        val drivetrain: DrivetrainComponent, val lift: LiftComponent,

        val driverHardware: DriverHardware
) {

    fun teleop() = launchAll(
            { forks.teleop(driverHardware) },
            { hooks.teleop(driverHardware, lift) },
            { winch.teleop(driverHardware) },
            { clamp.teleop(driverHardware) },
            { pivot.teleop(driverHardware) },
            { rollers.teleop(driverHardware) },
            { drivetrain.teleop(driverHardware, lift) },
            { lift.teleop(driverHardware) }
    ).also {
        HAL.observeUserProgramTeleop()
        System.gc()
    }

    fun backAndForthAuto() = scope.launch {
        drivetrain.driveStraight(
                8.Foot, 0.Degree,
                1.Inch, 2.Degree,
                2.FootPerSecondSquared,
                3.FootPerSecond
        )

//        delay(10000)
//
//        drivetrain.driveStraight(
//                -10.Foot, 0.Degree,
//                1.Inch, 2.Degree,
//                5.FootPerSecondSquared,
//                10.FootPerSecond
//        )
    }.also { HAL.observeUserProgramTeleop() }

    companion object : Named by Named("Subsystems Initializer") {
        fun concurrentInit() = runBlocking {
            val forks = async { ForksComponent(ForksHardware()) }
            val hooks = async { HooksComponent(HooksHardware()) }
            val winch = async { WinchComponent(WinchHardware()) }
            val clamp = async { ClampComponent(ClampHardware()) }
            val pivot = async { PivotComponent(PivotHardware()) }
            val rollers = async { RollersComponent(RollersHardware(), ElectricalSystemHardware()) }
            val drivetrain = async { DrivetrainComponent(DrivetrainHardware()) }
            val lift = async { LiftComponent(LiftHardware()) }
            val driver = async { DriverHardware() }

            Subsystems(
                    forks = forks.await(),
                    hooks = hooks.await(),
                    winch = winch.await(),
                    clamp = clamp.await(),
                    pivot = pivot.await(),
                    rollers = rollers.await(),
                    drivetrain = drivetrain.await(),
                    lift = lift.await(),
                    driverHardware = driver.await()
            )
        }

        fun init(): Subsystems {
            return Subsystems(
                    forks = ForksComponent(ForksHardware()),
                    hooks = HooksComponent(HooksHardware()),
                    winch = WinchComponent(WinchHardware()),
                    clamp = ClampComponent(ClampHardware()),
                    pivot = PivotComponent(PivotHardware()),
                    rollers = RollersComponent(RollersHardware(), ElectricalSystemHardware()),
                    drivetrain = DrivetrainComponent(DrivetrainHardware()),
                    lift = LiftComponent(LiftHardware()),
                    driverHardware = DriverHardware()
            )
        }
    }
}