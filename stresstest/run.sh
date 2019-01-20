#!/usr/bin/env bash

if [[ $# -eq 0 ]]
then
    echo "Stress test task not specified"
    exit
fi

osascript -e 'display notification "Running Stress Test" with title "Kapuchin Stress Testing"'

i="0"
while
$@  >> stresstest.log 2>&1
do :
i=$[$i+1]
done

osascript -e 'display notification "Stress Test Failed" with title "Kapuchin Stress Testing"'
echo "Test failed after $i trials."