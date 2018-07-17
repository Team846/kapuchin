package com.lynbrookrobotics.kapuchin.logging

expect class StackTraceElement

expect val Throwable.platformStackTrace: Array<StackTraceElement>