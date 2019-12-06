package com.altbeacon.altbeaconlibrary.beaconlibrary

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.RemoteException
import android.util.Log
import org.altbeacon.beacon.*

class BeaconScanManager private constructor(private val context: Context) : BeaconConsumer, RangeNotifier, MonitorNotifier {
    private val bolderRegion = Region(BeaconUUID, null, null, null)
    private val viddenRegion = Region(BeaconUUID1, null, null, null)
    private var beaconManager: BeaconManager? = null
    var isScanning = false
        private set

    companion object {

        private const val BeaconUUID = "b9407f30-f5f8-466e-aff9-25556b57fe6d"
        private const val BeaconUUID1 = "426F6C64-6572-2054-6563-680D0A0D0A0D"

        private var sInstance: BeaconScanManager? = null
        private var iBeaconMonitorCallbacks: IBeaconMonitorCallbacks? = null
        private var iBeaconRangeCallbacks: IBeaconRangeCallbacks? = null
        private var iBeaconConnectionCallbacks: IBeaconConnectionCallbacks? = null

        fun getInstance(context: Context): BeaconScanManager? {
            if (null == sInstance) {
                sInstance = BeaconScanManager(context)
            }
            if (context is IBeaconRangeCallbacks)
                iBeaconRangeCallbacks = context
            if (context is IBeaconConnectionCallbacks)
                iBeaconConnectionCallbacks = context
            if (context is IBeaconMonitorCallbacks)
                iBeaconMonitorCallbacks = context
            return sInstance
        }
    }

    val isBeaconRaging: Boolean
        get() = beaconManager != null && !beaconManager!!.rangedRegions.isEmpty()

    var beaconMonitorCallbacks: IBeaconMonitorCallbacks?
        get() = iBeaconMonitorCallbacks
        set(beaconMonitorCallbacks) {
            iBeaconMonitorCallbacks = beaconMonitorCallbacks
        }

    var beaconRangeCallbacks: IBeaconRangeCallbacks?
        get() = iBeaconRangeCallbacks
        set(beaconRangeCallbacks) {
            iBeaconRangeCallbacks = beaconRangeCallbacks
        }

    override fun onBeaconServiceConnect() {
        beaconManager!!.addMonitorNotifier(this)
        beaconManager!!.addRangeNotifier(this)
        startMonitoringRegion()
    }

    override fun getApplicationContext(): Context {
        return context
    }

    override fun unbindService(serviceConnection: ServiceConnection) {

    }

    override fun bindService(intent: Intent, serviceConnection: ServiceConnection, i: Int): Boolean {
        return false
    }

    override fun didRangeBeaconsInRegion(collection: Collection<Beacon>, region: Region) {
        if (iBeaconRangeCallbacks != null)
            iBeaconRangeCallbacks!!.didRangeBeaconsInRegion(collection, region)
    }

    override fun didEnterRegion(region: Region) {
        if (iBeaconMonitorCallbacks != null)
            iBeaconMonitorCallbacks!!.beaconDidEnterRegion(region)
    }

    override fun didExitRegion(region: Region) {
        if (iBeaconMonitorCallbacks != null)
            iBeaconMonitorCallbacks!!.beaconDidExitRegion(region)
    }

    override fun didDetermineStateForRegion(i: Int, region: Region) {
        if (iBeaconMonitorCallbacks != null)
            iBeaconMonitorCallbacks!!.beaconDidDetermineStateForRegion(i, region)
    }

    private fun startBeaconScan() {

        Log.d("BeaconScan", "BeaconScan")
        beaconManager = BeaconManager.getInstanceForApplication(context)
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // BeaconManager setup
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"))
        // Detect the main identifier (UID) frame:
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"))
        // Detect the telemetry (TLM) frame:
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"))
        // Detect the URL frame:
        beaconManager!!.beaconParsers.add(BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v"))

//         beaconManager.setDebug(true);
//         beaconManager?.backgroundScanPeriod = 10000
        // beaconManager.setBackgroundBetweenScanPeriod(30000l);
        //beaconManager.setBackgroundMode(false);
    }

    private fun bindBeaconConsumer() {
        // Log.d("BeaconConsumer", "" + beaconManager);
        isScanning = true
        Log.d("BeaconConsumer", "" + this)
        beaconManager!!.bind(this)

    }

    private fun unbindBeaconService() {
        Log.d("BeaconConsumer", "unbindBeaconService")
        if (beaconManager != null) {
            Log.d("BeaconConsumer", "" + this)
            try {
                stopMonitoringRegion()
                stopAllRangingRegions()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            beaconManager!!.unbind(this)
            // beaconManager.setBackgroundMode(false);
        }
        isScanning = false
    }

    private fun stopAllRangingRegions() {
        try {
            if (!beaconManager!!.rangedRegions.isEmpty()) {
                beaconManager!!.stopRangingBeaconsInRegion(bolderRegion)
                beaconManager!!.stopRangingBeaconsInRegion(viddenRegion)
            }

        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }


    fun startMonitoringRegion() {
        try {
            beaconManager!!.startMonitoringBeaconsInRegion(bolderRegion)
            beaconManager!!.startMonitoringBeaconsInRegion(viddenRegion)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun stopMonitoringRegion() {
        try {
            beaconManager!!.stopMonitoringBeaconsInRegion(bolderRegion)
            beaconManager!!.stopMonitoringBeaconsInRegion(viddenRegion)

        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun startScan() {
        startBeaconScan()
        bindBeaconConsumer()
    }

    fun stopScan() {
        unbindBeaconService()
    }

    fun startRangingRegion() {
        try {
            beaconManager?.startRangingBeaconsInRegion(bolderRegion)
            beaconManager?.startRangingBeaconsInRegion(viddenRegion)

        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    fun stopRangingRegion() {
        try {
            beaconManager!!.stopRangingBeaconsInRegion(bolderRegion)
            beaconManager!!.stopRangingBeaconsInRegion(viddenRegion)

        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }


}
