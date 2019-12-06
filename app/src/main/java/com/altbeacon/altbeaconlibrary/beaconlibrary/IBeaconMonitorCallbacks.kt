package com.altbeacon.altbeaconlibrary.beaconlibrary

import org.altbeacon.beacon.Region

interface IBeaconMonitorCallbacks {

    fun beaconDidEnterRegion(region: Region)
    fun beaconDidExitRegion(region: Region)
    fun beaconDidDetermineStateForRegion(i: Int, region: Region)
}
