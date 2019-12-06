package com.altbeacon.altbeaconlibrary.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.altbeacon.altbeaconlibrary.R
import org.altbeacon.beacon.Beacon

class BeaconListAdapter(val context: Context, private val beaconList: List<Beacon>): RecyclerView.Adapter<BeaconListAdapter.BeaconViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeaconViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.beacon_single_item_layout, parent, false)
        return BeaconViewHolder(v)
    }

    override fun getItemCount(): Int {
        return beaconList.size
    }

    override fun onBindViewHolder(holder: BeaconViewHolder, position: Int) {
        holder.tvBeaconName.text = beaconList[position].bluetoothName
        holder.tvBluetoothAddress.text = "Address : ${beaconList[position].bluetoothAddress}"
        holder.tvBeaconUUID.text = "UUID ${beaconList[position].identifiers[0]}"
        holder.tvMajorId.text = "MAJOR ID: ${beaconList[position].identifiers[1]}"
        holder.tvMinorId.text = "MINOR ID: ${beaconList[position].identifiers[2]} RSSI ${beaconList[position].rssi}"
    }




    class BeaconViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val tvBeaconName: TextView = itemView?.findViewById(R.id.tv_beacon_name)!!
        val tvBluetoothAddress: TextView = itemView?.findViewById(R.id.tv_bluetooth_address)!!
        val tvBeaconUUID: TextView = itemView?.findViewById(R.id.tv_beacon_uuid)!!
        val tvMajorId: TextView = itemView?.findViewById(R.id.tv_major_id)!!
        val tvMinorId: TextView = itemView?.findViewById(R.id.tv_minor_id)!!
    }
}