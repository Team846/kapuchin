package com.lynbrookrobotics.kapuchin.tests.hardware

import com.lynbrookrobotics.kapuchin.control.stampWith
import com.lynbrookrobotics.kapuchin.hardware.Sensor.Companion.sensor
import com.lynbrookrobotics.kapuchin.tests.`is equal to?`
import com.lynbrookrobotics.kapuchin.tests.`is greater than?`
import com.lynbrookrobotics.kapuchin.tests.subsystems.TC
import com.lynbrookrobotics.kapuchin.tests.subsystems.TSH
import com.lynbrookrobotics.kapuchin.timing.EventLoop
import com.lynbrookrobotics.kapuchin.timing.checkInSync
import com.lynbrookrobotics.kapuchin.timing.scope
import com.lynbrookrobotics.kapuchin.timing.currentTime
import info.kunalsheth.units.generated.avg
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SensorTest {

    private class SensorTestSH : TSH<SensorTestSH, SensorTestC>("SensorTest Hardware") {
        val sensorA = sensor { Math.random() stampWith currentTime }
        val sensorB = sensor { Math.random() stampWith currentTime }
    }

    private object SensorTestC : TC<SensorTestC, SensorTestSH>(SensorTestSH())

    @Test(timeout = 2 * 1000)
    fun `sensors read on tick are in sync`() = runBlocking {
        val name = "sensors read on tick are in sync"
        SensorTestC.run {
            startRoutine(name) {
                val a by hardware.sensorA.readOnTick.withStamps
                val b by hardware.sensorB.readOnTick.withStamps
                var runs = 10
                controller {
                    checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                    currentTime `is greater than?` a.stamp
                    currentTime `is greater than?` b.stamp
                    name.takeIf { runs-- > 0 }
                }
            }
        }
    }

    @Test(timeout = 3 * 1000)
    fun `sensors read on event loop are in sync`() = runBlocking {
        val name = "sensors read on tick are in sync"
        SensorTestC.run {
            startRoutine(name) {
                val a by hardware.sensorA.readWithEventLoop.withStamps
                val b by hardware.sensorB.readWithEventLoop.withStamps
                var lastStamp = currentTime
                var runs = 10
                controller {
                    if (runs % 2 == 0) EventLoop.tick(currentTime)

                    checkInSync(hardware.syncThreshold, a, b) `is equal to?` true
                    currentTime `is greater than?` a.stamp
                    currentTime `is greater than?` b.stamp

                    val thisStamp = avg(a.stamp, b.stamp)
                    if (runs % 2 == 1) lastStamp `is equal to?` thisStamp
                    lastStamp = thisStamp

                    name.takeIf { runs-- > 0 }
                }
            }
        }
    }

    @Test(timeout = 2 * 1000)
    fun `sensors read eagerly are eager and efficient`() = runBlocking {
        val name = "sensors read eagerly are eager and efficient"
        SensorTestC.run {
            startRoutine(name) {
                val a by hardware.sensorA.readEagerly.withStamps
                val b by hardware.sensorB.readEagerly.withStamps
                var runs = 10
                controller {
                    val a1 = a
                    val b1 = b
                    val a2 = a
                    val b2 = b

                    b2 `is equal to?` b1
                    b1.stamp `is greater than?` a2.stamp
                    a2 `is equal to?` a1

                    name.takeIf { runs-- > 0 }
                }
            }
        }
    }

    @Test(timeout = 2 * 1000)
    fun `sensors are read efficiently`() = runBlocking {
        val name = "sensors are read efficiently"
        SensorTestC.run {
            startRoutine(name) {
                val a1 by hardware.sensorA.readOnTick.withStamps
                val a2 by hardware.sensorA.readEagerly.withStamps
                val b1 by hardware.sensorB.readOnTick.withStamps
                val b2 by hardware.sensorB.readEagerly.withStamps
                var runs = 10
                controller {
                    a1 `is equal to?` a2
                    b1 `is equal to?` b2
                    name.takeIf { runs-- > 0 }
                }
            }
        }
    }

    @Test(timeout = 4 * 1000)
    fun `sensor lambdas are released upon routine completion`() = runBlocking {
        val name = "sensor lambdas are released upon routine completion"
        SensorTestC.run {

            val ogClockJobs = clock.jobs.size
            val ogElJobs = EventLoop.jobs.size
            fun check() {
                while (clock.jobs.size > ogClockJobs) Thread.sleep(1)
                while (EventLoop.jobs.size > ogElJobs) Thread.sleep(1)
            }

            suspend fun routine() = startRoutine(name) {
                val a1 by hardware.sensorA.readOnTick.withStamps
                val a2 by hardware.sensorA.readWithEventLoop.withStamps
                val a3 by hardware.sensorA.readEagerly.withStamps

                val a4 by hardware.sensorA.readOnTick.withoutStamps
                val a5 by hardware.sensorA.readWithEventLoop.withoutStamps
                val a6 by hardware.sensorA.readEagerly.withoutStamps

                val b1 by hardware.sensorB.readOnTick.withStamps
                val b2 by hardware.sensorB.readWithEventLoop.withStamps
                val b3 by hardware.sensorB.readEagerly.withStamps

                val b4 by hardware.sensorB.readOnTick.withoutStamps
                val b5 by hardware.sensorB.readWithEventLoop.withoutStamps
                val b6 by hardware.sensorB.readEagerly.withoutStamps

                var runs = 5
                controller {
                    a1.value `is equal to?` a4
                    a2.value `is equal to?` a5
                    a3.value `is equal to?` a6

                    b1.value `is equal to?` b4
                    b2.value `is equal to?` b5
                    b3.value `is equal to?` b6
                    name.takeIf { runs-- > 0 }
                }
            }

            suspend fun badRoutine() = startRoutine(name) {
                val a1 by hardware.sensorA.readOnTick.withStamps
                val a2 by hardware.sensorA.readWithEventLoop.withStamps
                val a3 by hardware.sensorA.readEagerly.withStamps

                val a4 by hardware.sensorA.readOnTick.withoutStamps
                val a5 by hardware.sensorA.readWithEventLoop.withoutStamps
                val a6 by hardware.sensorA.readEagerly.withoutStamps

                val b1 by hardware.sensorB.readOnTick.withStamps
                val b2 by hardware.sensorB.readWithEventLoop.withStamps
                val b3 by hardware.sensorB.readEagerly.withStamps

                val b4 by hardware.sensorB.readOnTick.withoutStamps
                val b5 by hardware.sensorB.readWithEventLoop.withoutStamps
                val b6 by hardware.sensorB.readEagerly.withoutStamps

                var runs = 5
                controller {
                    a1.value `is equal to?` a4
                    a2.value `is equal to?` a5
                    a3.value `is equal to?` a6

                    b1.value `is equal to?` b4
                    b2.value `is equal to?` b5
                    b3.value `is equal to?` b6
                    if (runs == 2) error("This routine is broken!")
                    name.takeIf { runs-- > 0 }
                }
            }

            routine()
            check()

            val j1 = scope.launch { routine() }
            while (routine == null) Thread.sleep(1)
            routine!!.cancel()
            check()

            val j2 = scope.launch { routine() }
            while (!j2.isActive) Thread.sleep(1)
            j2.cancel()
            check()

            val j3 = badRoutine()
            check()

            val j4 = scope.launch { badRoutine() }
            check()

            val j5 = scope.launch {
                routine()
                routine()
                routine()
            }
            while (routine == null) Thread.sleep(1)
            j5.cancel()
            check()


            val j6 = scope.launch {
                routine()
                routine()
                routine()
            }
            while (routine == null) Thread.sleep(1)
            routine!!.cancel()
            j6.join()
            check()
        }
    }
}