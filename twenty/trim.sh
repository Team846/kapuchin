#!/bin/bash

# @author Andy
#
# When booting up the robot, the file `keylist.txt` will be written to with all the preferences currently in use.
# Copy this script to the RoboRIO then run it to delete all unused preferences off of `networktables.ini`.
# Running this script with no flags will do a dry run. Pass `-f` to modify networktables.ini.
# To prevent the robot from starting up, `killall java` is run in various places.

# Mark 1 if testing on your own machine.
DEBUG=0


ini=$( (($DEBUG)) && echo "./networktables.ini" || echo "/home/lvuser/networktables.ini" )
keylist=$( (($DEBUG)) && echo "./keylist.txt" || echo "/home/lvuser/keylist.txt" )
out=$( (($DEBUG)) && echo "./output.txt" || echo "/home/lvuser/output.txt" )

#Check if -f was passed
f=0
while getopts ":f" opt; do
  case ${opt} in
    f )
      f=1
      ;;
  esac
done


touch output.txt

# For each line in networktables.ini
while read -r line
do
    ! (($DEBUG)) && killall java

    # Get rid of type in the beginning (first word)
    # Get rid of quotations in front and end
    # Delete `/Preferences/` (first 14 characters)
    key=`grep -o '".*"' <<< "$line" | sed 's/"//g' | cut -c 14-`

    # If key is found in the keylist
    if grep -q "$key" "$keylist"
    then
        if (($f))
        then
            echo "$line" >> "$out"
        fi
    else
        if (($f))
        then
            echo "Trimming $key"
        else
            echo "Would trim $key"
        fi
    fi
done < "$ini"


! (($DEBUG)) && killall java

if (($f))
then
    echo "
Copying output.txt to networktables.ini"
    cp -f "$out" "$ini"
    echo "Deleting output.txt"
    rm "$out"
else
    echo "
Run with -f to actually trim entries"
fi

! (($DEBUG)) && killall java