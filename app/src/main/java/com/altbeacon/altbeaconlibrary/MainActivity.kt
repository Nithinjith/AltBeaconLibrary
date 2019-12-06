package com.altbeacon.altbeaconlibrary


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.os.CountDownTimer
import android.os.RemoteException
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.altbeacon.altbeaconlibrary.adapters.BeaconListAdapter
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), BeaconConsumer, MonitorNotifier, RangeNotifier {

    companion object {
        private const val BeaconUUID = "ffffffff-1234-aaaa-1a2b-a1b2c3d4e5f6"
        private const val BeaconUUID1 = "12345678-abcd-88cc-abcd-1111aaaa2222"
    }

    private var adapter: BeaconListAdapter?  = null
    private var beaconList: MutableList<Beacon> = mutableListOf()

    private val geofenceList: ArrayList<Geofence> = arrayListOf()

    private val region1 = Region("Region1", listOf(Identifier.parse(BeaconUUID)))//Region( BeaconUUID, null, null)

    private val region2 = Region("Region2", listOf(Identifier.parse(BeaconUUID1)))

    private var isScanning: Boolean = false

    private var TAG = "MainActivity"

    private var beaconManager: BeaconManager? = null

    private lateinit var geofencingClient: GeofencingClient

    private lateinit var timer : MyTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkLocationPermission()

        btn_monitor.setOnClickListener {
            startMonitoringRegion()
        }

        btn_region_raning.setOnClickListener {
            startRangingRegion()
            startTimer()
        }

        btn_stop_monitoring.setOnClickListener {
            stopMonitoringRegion()
        }

        btn_stop_ranging.setOnClickListener {
            stopRangingRegion()
            stopTimer()
            clearList()
        }
        btn_stop_scan.setOnClickListener {
            stopScan()
        }
    }

    private fun clearList() {
        beaconList.clear()
        adapter?.notifyDataSetChanged()
    }

    private fun checkLocationPermission() {
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        Permissions.check(this/*context*/, permissions, null/*options*/, null, object : PermissionHandler() {
            override fun onGranted() {
                initializeGeoFence()
                initializeBeacon()
                startScan()
                setAdapter()
            }
        })
    }

    private fun startTimer(){
        timer = MyTimer(millisInFuture = 3000, countDownInterval = 1000)
        timer.start()
    }

    private fun stopTimer(){
        timer?.cancel()
    }

    private fun initializeGeoFence() {
        geofencingClient = LocationServices.getGeofencingClient(this)
    }

    private fun initializeBeacon() {
        setBluetooth()
    }

    private fun setBluetooth(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val isEnabled = bluetoothAdapter.isEnabled
        if (!isEnabled) {
            return bluetoothAdapter.enable()
        }
        // No need to change bluetooth state
        return true
    }

    override fun onBeaconServiceConnect() {
        Log.i(TAG, "onBeaconServiceConnect ")
        beaconManager?.addMonitorNotifier(this)
        beaconManager?.addRangeNotifier(this)
    }


    override fun didRangeBeaconsInRegion(beacon: MutableCollection<Beacon>?, region: Region?) {
        Log.i(TAG, "didRangeBeaconsInRegion region UUID: ${region?.id1} Major ID ${region?.id2} Minor Id ${region?.id3}")
        if (!beacon.isNullOrEmpty()) {
           if(beaconList.isEmpty()){
               beaconList.addAll(beacon)
           }else{
              var newAddressList = beacon.filter { !it.bluetoothAddress.isNullOrEmpty() }
                      .map { it.bluetoothAddress}
              var existingAddressList = beaconList.map { it.bluetoothAddress}

              var addedList = newAddressList.filter { !existingAddressList.contains(it)}

              var removeList = existingAddressList.filter { !newAddressList.contains(it) }

              for(item in beacon) {
                  if(addedList.contains(item.bluetoothAddress)){
                      beaconList.add(item)
                  }
              }
              for(address in removeList){

                  var removeObject: Beacon? = null
                  for(removeObj in beaconList){
                      if(removeObj.bluetoothAddress == address){
                          removeObject = removeObj
                          break
                      }
                  }
                  if(removeObject != null){
                      beaconList.remove(removeObject)
                  }
              }
           }
           sortList()
        }
    }

    private fun sortList() {
        beaconList.sortByDescending { beacon -> beacon.rssi}
    }


    override fun didDetermineStateForRegion(variable: Int, region: Region?) {
        Log.i(TAG, "didDetermineStateForRegion variable $variable  region ${region?.uniqueId} Major ID ${region?.id2} Minor Id ${region?.id3}")
    }

    override fun didEnterRegion(region: Region?) {
        Log.i(TAG, "didEnterRegion region ${region?.uniqueId} Major ID ${region?.id2} Minor Id ${region?.id3}")
    }

    override fun didExitRegion(region: Region?) {
        Log.w(TAG, "didExitRegion region ${region?.uniqueId} Major ID ${region?.id1} Minor Id ${region?.id2}")
    }

    private fun setAdapter(){
        adapter = beaconList?.let { BeaconListAdapter(context = this, beaconList = it) }
        rvBeaconList.adapter = adapter
        rvBeaconList.layoutManager = LinearLayoutManager(
                this,
                RecyclerView.VERTICAL, false
        )
    }

    inner class MyTimer(millisInFuture: Long, countDownInterval: Long) : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            notifyChanges()
            restartTimer()
        }
        override fun onTick(millisUntilFinished: Long) {
        }

    }

    private fun notifyChanges() {
        adapter?.notifyDataSetChanged()
    }

    fun restartTimer() {
       startTimer()
    }

    private fun startBeaconScan() {

        Log.i("BeaconScan", "BeaconScan")
        beaconManager = BeaconManager.getInstanceForApplication(this)
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        // BeaconManager setup
        beaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"))
        // Detect the main identifier (UID) frame:
        beaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"))
        // Detect the telemetry (TLM) frame:
        beaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout("x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15"))
        // Detect the URL frame:
        beaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-21v"))

    }

    private fun bindBeaconConsumer() {
        // Log.d("BeaconConsumer", "" + beaconManager);
        isScanning = true
        Log.i("BeaconConsumer", "" + this)
        beaconManager?.bind(this)

    }

    private fun unbindBeaconService() {
        Log.i("BeaconConsumer", "unbindBeaconService")
        if (beaconManager != null) {
            Log.i("BeaconConsumer", "" + this)
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
                beaconManager!!.stopRangingBeaconsInRegion(region1)
                beaconManager!!.stopRangingBeaconsInRegion(region2)
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }


    private fun startMonitoringRegion() {
        try {
            beaconManager?.startMonitoringBeaconsInRegion(region1)
            beaconManager?.startMonitoringBeaconsInRegion(region2)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun stopMonitoringRegion() {
        try {
            beaconManager!!.stopMonitoringBeaconsInRegion(region1)
            beaconManager!!.stopMonitoringBeaconsInRegion(region2)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun startScan() {
        startBeaconScan()
        bindBeaconConsumer()
    }

    fun stopScan() {
        unbindBeaconService()
    }

    private fun startRangingRegion() {
        try {
            beaconManager?.startRangingBeaconsInRegion(region1)
            beaconManager?.startRangingBeaconsInRegion(region2)

        } catch (e: RemoteException) {
            e.printStackTrace()
        }

    }

    private fun stopRangingRegion() {
        try {
            beaconManager?.stopRangingBeaconsInRegion(region1)
            beaconManager?.stopRangingBeaconsInRegion(region2)

        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//        builder.addGeofences(mGeofenceList)
        return builder.build()
    }
}
