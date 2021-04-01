package com.lynbrookrobotics.kapuchin.control.data

data class FourSided<out T>(val TR: T, val TL: T, val BL: T, val BR: T) {
}