package com.altbeacon.altbeaconlibrary.beaconlibrary

import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.Region

interface IBeaconRangeCallbacks {

    fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region)
}
