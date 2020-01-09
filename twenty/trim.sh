#!/bin/bash

ini="./networktables.ini"
keylist="./keylist.txt"
out="./output.txt"
# ini="/home/lvuser/networktables.ini"
# keylist="/home/lvuser/keylist.txt"
# out="/home/lvuser/output.txt"
f=false

#Check if -f was passed
while getopts ":f" opt; do
  case ${opt} in
    f )
      f=true
      ;;
  esac
done



while read -r line
do
    killall java
    if grep -q 'NetworkTables' <<< "$line"
    then
        continue
    fi

    key=`grep -o '".*"' <<< "$line" | sed 's/"//g' | cut -c 14-`

    if grep -q "$key" "$keylist"
    then
        if [ $f = false ]
        then
            echo "$line" >> "$out" 
        fi
    else
        if [ $f = true ]
        then
            echo "Trimming $key"    
        else
            echo "Would trim $line"
        fi
    fi
done < "$ini" | sed '1d'


killall java
if [ $f = true ]
then
    echo "Copying output.txt to networktables.ini"

    cp -f "$out" "$ini"
    echo "[NetworkTables Storage 3.0]
$(cat $ini)" > "$ini"
    echo "Deleting output.txt"
    rm "$out"
else
    echo "Run with -f to trim"
fi
killall java