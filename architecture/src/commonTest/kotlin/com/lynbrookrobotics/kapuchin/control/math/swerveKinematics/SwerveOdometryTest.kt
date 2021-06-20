package com.lynbrookrobotics.kapuchin.control.math.swerveKinematics
import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.control.math.drivetrain.swerve.*
import info.kunalsheth.units.generated.*
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertEquals

class SwerveOdometryTest {

    private val odom = SwerveOdometry(Position(0.Inch,0.Inch,0.Radian), 2.Foot, 2.Foot)

    @Test
    fun moveStraightLine(){
        val modules = arrayOf(Pair(5.Foot, 0.Radian), Pair(5.Foot, 0.Radian), Pair(5.Foot, 0.Radian), Pair(5.Foot, 0.Radian))
        val pos = odom.updatePosition(modules)
        println(pos)
        assertEquals(Position(5.Foot, 0.Foot, 0.Degree), pos, "Failed to reach correct destination. Expected: (5, 0, 90), but received ${pos}")
    }

    @Test
    fun moveDiagonally(){
        val modules = arrayOf(Pair(5.Foot, 45.Degree), Pair(5.Foot, 45.Degree), Pair(5.Foot, 45.Degree), Pair(5.Foot, 45.Degree))
        odom.setPosition(Position(0.Foot, 0.Foot, 0.Radian))
        val pos = odom.updatePosition(modules);
        println(pos)
        assertEquals(Position((5 / sqrt(2.0)).Foot, (5 / sqrt(2.0)).Foot, 90.Degree), pos, "expected (3.54, 3.54, 90), but received ${pos}") //works but rounding error doesn't let it. need to fix
    }

    @Test
    fun rotateInCircle(){
        val modules = arrayOf(Pair(3.Foot, 225.Degree), Pair(3.Foot, (-45).Degree), Pair(3.Foot, 45.Degree), Pair(3.Foot, 135.Degree))
        odom.setPosition(Position(0.Foot, 0.Foot, 0.Radian))
        val pos = odom.updatePosition(modules);
        println(pos);
        assertEquals(pos?.x, 0.0.Foot, "Was not 0");
        assertEquals(pos?.y, 0.0.Foot, "Was not 0");
    }

    @Test
    fun moveForwardThenTurn(){
        var modules = arrayOf(Pair(5.Foot, 0.Degree), Pair(5.Foot, 0.Degree), Pair(5.Foot, 0.Degree), Pair(5.Foot, 0.Degree))
        odom.setPosition(Position(0.Foot, 0.Foot, 0.Radian))
        val p1 = odom.updatePosition(modules);
        println(p1);
        modules = arrayOf(Pair(5.Foot, 90.Degree), Pair(5.Foot, 90.Degree), Pair(5.Foot, 90.Degree), Pair(5.Foot, 90.Degree))
        val pos = odom.updatePosition(modules);
        println(pos);
        assertEquals(Position(5.Foot, 5.Foot, 0.Degree), pos, "Failed to reach position");
    }

}