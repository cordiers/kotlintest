package fr.strada.screens.home


import android.content.Context

import android.os.*

import androidx.appcompat.app.AppCompatActivity

import fr.strada.R
import fr.strada.utils.*
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.util.Log
import android.widget.Toast
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothDevice
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import android.widget.ListView
import kotlinx.android.synthetic.main.activity_scan.*
import me.aflak.bluetooth.Bluetooth
import me.aflak.bluetooth.interfaces.BluetoothCallback
import me.aflak.bluetooth.interfaces.DiscoveryCallback

import fr.strada.screens.home.ReaderActivityKotlin.Companion.state
import me.aflak.bluetooth.interfaces.DeviceCallback


class ReaderActivityBluetoothKotlin : AppCompatActivity() {

    private var bluetooth: Bluetooth? = null
    private var scanListAdapter: ArrayAdapter<String>? = null
    private var pairedListAdapter: ArrayAdapter<String>? = null
    private var pairedDevices: List<BluetoothDevice> = ArrayList()
    private var scannedDevices: ArrayList<BluetoothDevice> = ArrayList()
    private var scanning = false


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        // list for paired devices
        pairedListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        pairedDeviceList.adapter = pairedListAdapter
        pairedDeviceList.onItemClickListener = onPairedListItemClick

        // list for scanned devices
        scanListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList())
        deviceList.adapter = scanListAdapter
        deviceList.onItemClickListener = onScanListItemClick

        // bluetooth lib
        bluetooth = Bluetooth(this)
        bluetooth!!.setCallbackOnUI(this)
        bluetooth!!.setBluetoothCallback(bluetoothCallback)
        bluetooth!!.setDiscoveryCallback(discoveryCallback)
        bluetooth!!.setDeviceCallback(deviceCallback);

        activity_scan_button.setOnClickListener {
            bluetooth!!.startScanning();
        }
        // ui...
        setProgressAndState("", View.GONE)
        activity_scan_button.setEnabled(false)
    }

    private val onPairedListItemClick =
        AdapterView.OnItemClickListener { adapterView, view, i, l ->
            if (scanning) {
                bluetooth!!.stopScanning()
            }
            startChatActivity(pairedDevices.get(i))
        }

    private fun setProgressAndState(msg: String, p: Int) {
        activity_scan_state.text = msg
        activity_scan_progress.visibility = p
    }


    override fun onStart() {
        super.onStart()
        bluetooth!!.onStart()
        if (bluetooth!!.isEnabled()) {
            displayPairedDevices()
            activity_scan_button.setEnabled(true)
        } else {
            bluetooth!!.showEnableDialog(this@ReaderActivityBluetoothKotlin)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        bluetooth!!.onActivityResult(requestCode, resultCode)
    }

    private fun displayPairedDevices() {
        pairedDevices = bluetooth!!.getPairedDevices()
        for (device in pairedDevices!!) {
            pairedListAdapter!!.add(device.address + " : " + device.name)
        }
    }

    private val onScanListItemClick =
        AdapterView.OnItemClickListener { adapterView, view, i, l ->
            if (scanning) {
                bluetooth!!.stopScanning()
            }
            setProgressAndState("Pairing...", View.VISIBLE)
            bluetooth!!.pair(scannedDevices!!.get(i))
        }

    override fun onStop() {
        super.onStop()
        bluetooth!!.onStop()
    }


    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    private fun showLog(msg: String) {
        Log.d("Reader",msg)
    }


    private fun startChatActivity(device : BluetoothDevice){
        showLog("device"+ device.name);
        bluetooth!!.connectToDevice(device);
    }


    private val bluetoothCallback = object : BluetoothCallback {
        override fun onBluetoothTurningOn() {}

        override fun onBluetoothOn() {
            displayPairedDevices()
            activity_scan_button.setEnabled(true)
        }

        override fun onBluetoothTurningOff() {
            activity_scan_button.setEnabled(false)
        }

        override fun onBluetoothOff() {}

        override fun onUserDeniedActivation() {
            Toast.makeText(this@ReaderActivityBluetoothKotlin, "I need to activate bluetooth...", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val discoveryCallback = object : DiscoveryCallback {
        override fun onDiscoveryStarted() {
            setProgressAndState("Scanning...", View.VISIBLE)
            scannedDevices = ArrayList()
            scanning = true
        }

        override fun onDiscoveryFinished() {
            setProgressAndState("Done.", View.INVISIBLE)
            scanning = false
        }

        override fun onDeviceFound(device: BluetoothDevice) {
            scannedDevices.add(device)
            scanListAdapter!!.add(device.address + " : " + device.name)
        }

        override fun onDevicePaired(device: BluetoothDevice) {
            Toast.makeText(this@ReaderActivityBluetoothKotlin, "Paired !", Toast.LENGTH_SHORT).show()
            startChatActivity(device)
        }

        override fun onDeviceUnpaired(device: BluetoothDevice) {

        }

        override fun onError(errorCode: Int) {

        }
    }

    private val deviceCallback = object : DeviceCallback {
        override fun onDeviceConnected(device: BluetoothDevice) {
            activity_scan_state.setText("Connected !")
            activity_scan_button.setEnabled(true)
        }

        override fun onDeviceDisconnected(device: BluetoothDevice, message: String) {
            activity_scan_state.setText("Device disconnected !")
            activity_scan_button.setEnabled(false)
        }

        override fun onMessage(message: ByteArray) {
            val str = String(message)
            activity_scan_state.setText("<- $str")
        }

        override fun onError(errorCode: Int) {

        }

        override fun onConnectError(device: BluetoothDevice, message: String) {
            activity_scan_state.setText("Could not connect, next try in 3 sec...")
            Handler().postDelayed({ bluetooth!!.connectToDevice(device) }, 3000)
        }
    }

}
