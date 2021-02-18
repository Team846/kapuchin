#!/bin/sh

# CTRE
curl "http://devsite.ctr-electronics.com/maven/development/com/ctre/phoenix/frcjson/5.19.5-rc-1/frcjson-5.19.5-rc-1-5.json" > Phoenix-latest.json

# REV
curl "https://www.revrobotics.com/content/sw/max/sdk/REVRobotics.json" > REVRobotics.json

# NAVX
curl "https://www.kauailabs.com/dist/frc/2021/navx_frc.json" > navx_frc.json

# Rev Color Sensor (v3)
curl "https://www.revrobotics.com/content/sw/color-sensor-v3/sdk/REVColorSensorV3.json" > REVColorSensorV3.json