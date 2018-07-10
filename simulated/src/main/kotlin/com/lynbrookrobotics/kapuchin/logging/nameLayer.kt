package com.lynbrookrobotics.kapuchin.logging

actual fun nameLayer(parent: Named?, child: String): String = "${parent?.name?.plus('/') ?: ""}$child"