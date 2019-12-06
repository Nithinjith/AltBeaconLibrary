package com.altbeacon.altbeaconlibrary.beaconlibrary

data class BeaconModel(val beaconName: String,
                       val beaconAddress: String,
                       val beaconUUID: String,
                       val beaconMajorId: String,
                       val beaconMinorId: String,
                       val beaconLastUpdateTime : String) {
}