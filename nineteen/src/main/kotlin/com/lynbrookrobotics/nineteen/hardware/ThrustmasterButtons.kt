package com.lynbrookrobotics.nineteen.hardware

import edu.wpi.first.wpilibj.Joystick

enum class ThrustmasterButtons(val raw: Int) {
    Trigger(1),
    LeftTrigger(3), BottomTrigger(2), RightTrigger(4),

    LeftOne(5), LeftTwo(6), LeftThree(7),
    LeftFour(10), LeftFive(9), LeftSix(8),


    RightThree(13), RightTwo(12), RightOne(11),
    RightSix(14), RightFive(15), RightFour(16),
}

operator fun Joystick.get(button: ThrustmasterButtons) = getRawButton(button.raw)