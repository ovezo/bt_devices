package ai.robominder.bt_devices.bluetooth

import ai.robominder.bt_devices.R
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class BluetoothListActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 101;

    private val bluetoothManager by lazy {
        getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    lateinit var bluetoothOffView: View
    lateinit var turnOnButton: Button
    lateinit var swipeRefreshLayout: SwipeRefreshLayout
    lateinit var bluetoothPairedRecyclerView: RecyclerView
    lateinit var bluetoothFoundRecyclerView: RecyclerView
    lateinit var btFoundAdapter: BluetoothRecyclerAdapter
    lateinit var pairedAdapter: BluetoothRecyclerAdapter

    val foundDevices: MutableSet<BluetoothDeviceItem> = mutableSetOf()

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bluetooth_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh)
        bluetoothFoundRecyclerView = findViewById(R.id.bluetooth_found_list)
        btFoundAdapter = BluetoothRecyclerAdapter()
        bluetoothFoundRecyclerView.adapter = btFoundAdapter

        bluetoothPairedRecyclerView = findViewById(R.id.bluetooth_paired_list)
        pairedAdapter = BluetoothRecyclerAdapter()
        bluetoothPairedRecyclerView.adapter = pairedAdapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device does not supports bluetooth!", Toast.LENGTH_SHORT).show()
        }

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { /* Not needed */ }

        val isBluetoothEnabled: Boolean = bluetoothAdapter?.isEnabled == true

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnableBluetooth = if(SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true

            if(canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            }
        }

        if(SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            )
        }


        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(receiver, filter)

        findDevices()

        swipeRefreshLayout.setOnRefreshListener {
            findDevices()
        }
    }

    @SuppressLint("MissingPermission")
    private fun findDevices(){
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        if (pairedDevices != null) {
            pairedAdapter.differ.submitList(pairedDevices.map { it.toBluetoothDeviceDomain(BTType.PAIRED) })
        }
        if (bluetoothAdapter?.isDiscovering == false)
            bluetoothAdapter?.startDiscovery()
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                    if (device != null) {
                        foundNewDevice(device.toBluetoothDeviceDomain(BTType.FOUND))
                    }

                    Log.d("mlog", "FOUND Device: ${device?.name} - ${device?.address}")
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    swipeRefreshLayout.isRefreshing = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun foundNewDevice(device: BluetoothDeviceItem){
        foundDevices.add(device)
        btFoundAdapter.differ.submitList(foundDevices.toList())

    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }

}