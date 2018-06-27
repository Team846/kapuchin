package com.lynbrookrobotics.kapuchin.logging

abstract class Named(name: String, parent: Named? = null) {
    open val name = nameLayer(parent, name)
}

expect fun nameLayer(parent: Named?, child: String): String