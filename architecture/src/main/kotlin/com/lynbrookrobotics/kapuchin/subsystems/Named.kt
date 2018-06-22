package com.lynbrookrobotics.kapuchin.subsystems

abstract class Named(parent: Named? = null, name: String) {
    open val name = nameLayer(parent, name)
}

expect fun nameLayer(parent: Named?, child: String): String