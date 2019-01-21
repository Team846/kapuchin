#!/usr/bin/env bash

if [[ $# -eq 0 ]]
then
    echo "Benchmark name not specified"
    exit
fi

KAPUCHIN="$PWD"
BENCHMARKS="benchmark"
BOOT_TIME=30
SLEEP_TIME=150

osascript -e 'display notification "Deploying Benchmark" with title "Kapuchin Realtime Tuning"'

cd "$KAPUCHIN"
./gradlew :nineteen:deploy
ssh -t admin@roboRIO-846-FRC.local reboot

osascript -e 'display notification "Running Benchmark" with title "Kapuchin Realtime Tuning"'

cd "$BENCHMARKS"
mkdir "$1"

sleep "$BOOT_TIME"
ssh -t lvuser@roboRIO-846-FRC.local tail -f FRC_UserProgram.log &
TASK_PID=$!
sleep "$SLEEP_TIME"
kill "$TASK_PID"

scp "lvuser@roboRIO-846-FRC.local:/tmp/*.csv" "$1"
scp "lvuser@roboRIO-846-FRC.local:~/FRC_UserProgram.log" "$1/FRC_UserProgram.log.csv"

osascript -e 'display notification "Finished Benchmark" with title "Kapuchin Realtime Tuning"'

GRAPHS="$1/GRAPHS_$(date +%s).xlsx"

cp GRAPHS.xlsx "$GRAPHS"
open "$1"/*.csv
open "$GRAPHS"