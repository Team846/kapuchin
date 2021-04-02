package com.lynbrookrobotics.kapuchin.control.data

data class FourSided<out T>(val topRight: T, val topLeft: T, val bottomRight: T, val bottomLeft: T) {
    constructor(allSides: T) : this(allSides, allSides, allSides, allSides)
}