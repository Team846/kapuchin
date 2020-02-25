#!/bin/bash


sed <"$1" "s/^roi_x:.*/roi_x:1/" | sed "s/^pipeline_res:.*/pipeline_res:2/" | sed "s/^roi_y:.*/roi_y:0/" >zoomInPanHigh.vpr
sed <"$1" "s/^roi_x:.*/roi_x:-1/" | sed "s/^pipeline_res:.*/pipeline_res:2/" | sed "s/^roi_y:.*/roi_y:0/" >zoomInPanLow.vpr
sed <"$1" "s/^roi_x:.*/roi_x:0/" | sed "s/^pipeline_res:.*/pipeline_res:1/" | sed "s/^roi_y:.*/roi_y:0/" >zoomOut.vpr
sed <"$1" "s/^roi_x:.*/roi_x:0/" | sed "s/^pipeline_res:.*/pipeline_res:2/" | sed "s/^roi_y:.*/roi_y:0/" >zoomInPanMid.vpr

