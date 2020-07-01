package fr.strada.screens.home

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import fr.strada.R

import java.util.LinkedList

class LeDeviceListAdapter(private val mContext: Activity) : BaseAdapter() {

    // Adapter for holding devices found through scanning.

    val allDeivces: LinkedList<BluetoothDevice>
    private val mInflator: LayoutInflater

    init {
        allDeivces = LinkedList()
        mInflator = mContext.layoutInflater
    }

    fun addDevice(device: BluetoothDevice) {
        if (!allDeivces.contains(device)) {// 注意:需判断重复
            allDeivces.add(device)
        }
    }

    fun getDevice(position: Int): BluetoothDevice {
        return allDeivces[position]
    }

    fun clear() {
        allDeivces.clear()
    }

    override fun getCount(): Int {
        return allDeivces.size
    }

    override fun getItem(i: Int): Any {
        return allDeivces[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val viewHolder: ViewHolder
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null)
            viewHolder = ViewHolder()
            viewHolder.deviceAddress = view!!
                .findViewById<View>(R.id.device_address) as TextView
            viewHolder.deviceName = view
                .findViewById<View>(R.id.device_name) as TextView
            view.tag = viewHolder
        } else {
            viewHolder = view.tag as ViewHolder
        }

        val device = allDeivces[i]
        val deviceName = device.name
        if (deviceName != null && deviceName.length > 0)
            viewHolder.deviceName!!.text = deviceName
        else
            viewHolder.deviceName!!.text = "unknown"
        viewHolder.deviceAddress!!.text = device.address

        return view
    }

    internal inner class ViewHolder {
        var deviceName: TextView? = null
        var deviceAddress: TextView? = null
    }
}
