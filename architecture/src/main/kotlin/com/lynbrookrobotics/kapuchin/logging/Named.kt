package com.lynbrookrobotics.kapuchin.logging

interface Named {
    val name: String

    companion object {
        operator fun invoke(name: String, parent: Named? = null) = object : Named {
            override val name = nameLayer(parent, name)
        }
    }
}

expect fun nameLayer(parent: Named?, child: String): String