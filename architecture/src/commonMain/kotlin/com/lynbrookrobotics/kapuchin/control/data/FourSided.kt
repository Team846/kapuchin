package com.lynbrookrobotics.kapuchin.control.data

data class FourSided<out T>(val frontRight: T, val frontLeft: T, val backRight: T, val backLeft: T) {
    constructor(allSides: T) : this(allSides, allSides, allSides, allSides)
}