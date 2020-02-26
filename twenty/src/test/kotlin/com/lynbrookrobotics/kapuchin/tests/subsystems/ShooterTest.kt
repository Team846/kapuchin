package com.lynbrookrobotics.kapuchin.tests.subsystems

import com.lynbrookrobotics.kapuchin.logging.*

class ShooterTest : Named by Named("Shooter Test") {

    // Travis breaks when running this class of tests

    /*
    private val launchAngleUp = 40.Degree
    private val launchAngleDown = 70.Degree
    private val maxSpeed = 9632.Rpm
    private val momentFactor = 1.4
    private val rollerRadius = 2.Inch
    private val momentOfInertia = 1.2.PoundFootSquared
    private val fudgeFactor = 100.Percent
    private val shooterHeight = 24.Inch

    private fun bestShotFrom(dist: Length) = bestShot(
            DetectedTarget(Position(0.Foot, dist + innerGoalDepth, 0.Degree), Position(0.Foot, dist, 0.Degree)), true,
            launchAngleUp, launchAngleDown,
            maxSpeed, momentFactor, rollerRadius, momentOfInertia, fudgeFactor, shooterHeight
    ).also {
        println("Target RPM: ${it?.flywheel?.Rpm}")
        println("Hood: ${it?.hood}")
        println("Goal: ${it?.goal}")
        println("Entry Angle: ${it?.entryAngle?.Degree} deg")

    }

    @Test
    fun `shot from 10 ft is feasible`() {
        val shot = bestShotFrom(10.Foot)

        assert(shot != null) { "Shot should be possible" }
        assert(shot!!.flywheel in 2000.Rpm..10000.Rpm) { "Shot should be within 2000-10000 rpm" }
    }

    @Test
    fun `shot from 1000 ft is not possible`() {
        val shot = bestShotFrom(1000.Foot)

        assert(shot == null) { "Shot should not be possible" }
    }

    @Test
    fun `shot from super close ft is not possible`() {
        val shot = bestShotFrom(0.1.Foot)

        assert(shot == null) { "Shot should not be possible" }
    }

    @Test
    fun `close shot sets hood down`() {
        val shot = bestShotFrom(6.Foot)

        assert(shot != null) { "Shot should be possible" }
        assert(shot!!.hood == Down) { "Hood should be down" }
    }

    @Test
    fun `far shot sets hood up`() {
        val shot = bestShotFrom(20.Foot)

        assert(shot != null) { "Shot should be possible" }
        assert(shot!!.hood == Up) { "Hood should be up" }
    }

//    @Test
//    fun `reasonable shot aims for inner goal`() {
//        val shot = bestShotFrom(15.Foot)
//
//        assert(shot != null) { "Shot should be possible" }
//        assert(shot!!.goal == Inner) { "Should aim for inner"}
//    }
//
//    @Test
//    fun `close shot aims for outer goal`() {
//        val shot = bestShotFrom(3.Foot)
//
//        assert(shot != null) { "Shot should be possible" }
//        assert(shot!!.goal == Outer) { "Should aim for Outer"}
//    }
*/

}