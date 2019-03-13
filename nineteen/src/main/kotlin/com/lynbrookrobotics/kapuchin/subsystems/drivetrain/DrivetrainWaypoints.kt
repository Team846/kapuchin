package com.lynbrookrobotics.kapuchin.subsystems.drivetrain

import com.lynbrookrobotics.kapuchin.control.data.*
import com.lynbrookrobotics.kapuchin.logging.*
import com.lynbrookrobotics.kapuchin.preferences.*
import info.kunalsheth.units.generated.*

class DrivetrainWaypoints(val hardware: DrivetrainHardware) : Named by Named("Waypoints", hardware) {

    val leftHab by pref {
        val x by pref(9, Foot)
        val y by pref(6, Foot)
        val bearing by pref(0, Degree)
        ({ Position(x, y, bearing) })
    }

    val rightHab by pref {
        val x by pref(9, Foot)
        val y by pref(6, Foot)
        val bearing by pref(0, Degree)
        ({ Position(x, y, bearing) })
    }

    val leftLoadingStation by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(180, Degree)
        ({ Position(x, y, bearing) })
    }

    val rightLoadingStation by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(180, Degree)
        ({ Position(x, y, bearing) })
    }

    val leftRocketFront by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(-45, Degree)
        ({ Position(x, y, bearing) })
    }

    val rightRocketFront by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(45, Degree)
        ({ Position(x, y, bearing) })
    }

    val leftRocketBack by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(225, Degree)
        ({ Position(x, y, bearing) })
    }

    val rightRocketBack by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(135, Degree)
        ({ Position(x, y, bearing) })
    }

    val leftCargoClose by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(90, Degree)
        ({ Position(x, y, bearing) })
    }

    val rightCargoMiddle by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(-90, Degree)
        ({ Position(x, y, bearing) })
    }

    val leftCargoFar by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(90, Degree)
        ({ Position(x, y, bearing) })
    }

    val rightCargoClose by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(-90, Degree)
        ({ Position(x, y, bearing) })
    }

    val leftCargoMiddle by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(90, Degree)
        ({ Position(x, y, bearing) })
    }

    val rightCargoFar by pref {
        val x by pref(3, Foot)
        val y by pref(3, Foot)
        val bearing by pref(-90, Degree)
        ({ Position(x, y, bearing) })
    }
}