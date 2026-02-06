package com.example.rgbsensorble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "RGBSensorBLE"
        private const val TARGET_DEVICE_NAME = "ESP32_Yellow_Sensor"
        
        private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        private val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        private val DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        
        private const val REQUEST_PERMISSIONS = 100
        private const val SCAN_PERIOD: Long = 10000
    }
    
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    
    private lateinit var btnScan: Button
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvRValue: TextView
    private lateinit var tvGValue: TextView
    private lateinit var tvBValue: TextView
    private lateinit var colorPreview: View
    
    private var isScanning = false
    private var isConnected = false
    private var discoveredDevice: BluetoothDevice? = null
    
    private val handler = Handler(Looper.getMainLooper())

    // 블루투스 활성화 요청을 위한 Launcher
    private val enableBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            startScan()
        } else {
            showToast("블루투스를 켜야 기기를 찾을 수 있습니다.")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initBluetooth()
        checkPermissions()
    }
    
    private fun initViews() {
        btnScan = findViewById(R.id.btnScan)
        btnConnect = findViewById(R.id.btnConnect)
        btnDisconnect = findViewById(R.id.btnDisconnect)
        tvStatus = findViewById(R.id.tvStatus)
        tvRValue = findViewById(R.id.tvRValue)
        tvGValue = findViewById(R.id.tvGValue)
        tvBValue = findViewById(R.id.tvBValue)
        colorPreview = findViewById(R.id.colorPreview)
        
        btnScan.setOnClickListener { startScan() }
        btnConnect.setOnClickListener { connectToDevice() }
        btnDisconnect.setOnClickListener { disconnectDevice() }
        
        updateUI()
    }
    
    private fun initBluetooth() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    }
    
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissions.isNotEmpty()) ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
    }
    
    private fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    
    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (!hasScanPermission()) { checkPermissions(); return }
        
        // 블루투스가 꺼져있는지 확인
        if (bluetoothAdapter == null || bluetoothAdapter?.isEnabled == false) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        if (isScanning) { stopScan(); return }
        
        val scanner = bluetoothLeScanner ?: return
        isScanning = true
        discoveredDevice = null
        btnScan.setText(R.string.btn_scan_stop)
        tvStatus.text = "센서 검색 중..."
        
        // ESP32가 광고에 UUID를 포함하지 않을 수 있으므로 필터 없이 스캔 후 이름으로 구분
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        handler.postDelayed({ if (isScanning) stopScan() }, SCAN_PERIOD)
        
        try {
            scanner.startScan(null, settings, scanCallback)
            Log.d(TAG, "Scan started")
        } catch (e: Exception) {
            Log.e(TAG, "Scan error: ${e.message}")
            showToast("스캔 시작 실패")
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun stopScan() {
        if (!isScanning) return
        isScanning = false
        btnScan.setText(R.string.btn_scan_start)
        try { bluetoothLeScanner?.stopScan(scanCallback) } catch (e: Exception) {}
        updateUI()
    }
    
    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val deviceName = result.device.name
            val deviceAddress = result.device.address
            Log.d(TAG, "Found device: $deviceName ($deviceAddress)")

            // 이름이 일치하는 기기만 선택
            if (deviceName == TARGET_DEVICE_NAME) {
                discoveredDevice = result.device
                Log.d(TAG, "Target device matched!")
                
                runOnUiThread {
                    tvStatus.text = "센서 발견! 연결 시도..."
                    stopScan()
                    connectToDevice()
                }
            }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun connectToDevice() {
        val device = discoveredDevice ?: return
        if (!hasConnectPermission()) { checkPermissions(); return }
        
        bluetoothGatt?.close()
        bluetoothGatt = null
        
        tvStatus.text = "GATT 연결 시도..."
        try {
            // TRANSPORT_LE를 명시적으로 지정하여 연결 안정성 확보
            bluetoothGatt = device.connectGatt(this, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
            Log.d(TAG, "connectGatt called for ${device.address}")
        } catch (e: SecurityException) {
            showToast("연결 권한이 없습니다")
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun disconnectDevice() {
        bluetoothGatt?.disconnect()
    }
    
    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange: status=$status, newState=$newState")
            
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "GATT Error status: $status")
                gatt.close()
                if (gatt == bluetoothGatt) bluetoothGatt = null
                runOnUiThread {
                    isConnected = false
                    updateUI()
                    tvStatus.text = "연결 오류 (Code: $status). 다시 시도하세요."
                }
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.")
                isConnected = true
                runOnUiThread { 
                    tvStatus.setText(R.string.status_connected)
                    updateUI()
                }
                // MTU 요청 (데이터 전송 전 필수 단계는 아니나 안정성을 위해 권장)
                gatt.requestMtu(512)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.")
                isConnected = false
                gatt.close()
                if (gatt == bluetoothGatt) bluetoothGatt = null
                runOnUiThread { 
                    tvStatus.setText(R.string.status_disconnected)
                    updateUI()
                    resetRGBValues()
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(TAG, "MTU changed: $mtu, status: $status")
            // 서비스 발견은 약간의 지연 후 실행하는 것이 안정적임
            handler.postDelayed({ gatt.discoverServices() }, 600)
        }
        
        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
                
                if (characteristic != null) {
                    Log.d(TAG, "Characteristic found, enabling notifications...")
                    enableNotification(gatt, characteristic)
                } else {
                    Log.e(TAG, "Characteristic NOT found in service!")
                    runOnUiThread { tvStatus.text = "특성(Characteristic)을 찾을 수 없음" }
                }
            } else {
                Log.e(TAG, "Service discovery failed with status: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            processBinaryData(value)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            characteristic.value?.let { processBinaryData(it) }
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID)
        if (descriptor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
            Log.d(TAG, "Notification enabled descriptor written")
        } else {
            Log.e(TAG, "Descriptor 2902 not found!")
        }
    }
    
    private fun processBinaryData(data: ByteArray) {
        if (data.size < 6) return
        
        try {
            val rawR = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
            val rawG = ((data[2].toInt() and 0xFF) shl 8) or (data[3].toInt() and 0xFF)
            val rawB = ((data[4].toInt() and 0xFF) shl 8) or (data[5].toInt() and 0xFF)
            
            val r8 = ((rawR.toLong() * 255) / 65535).toInt().coerceIn(0, 255)
            val g8 = ((rawG.toLong() * 255) / 65535).toInt().coerceIn(0, 255)
            val b8 = ((rawB.toLong() * 255) / 65535).toInt().coerceIn(0, 255)

            runOnUiThread {
                tvRValue.text = "R: $rawR"
                tvGValue.text = "G: $rawG"
                tvBValue.text = "B: $rawB"
                colorPreview.setBackgroundColor(Color.rgb(r8, g8, b8))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Data parse error: ${e.message}")
        }
    }
    
    private fun resetRGBValues() {
        tvRValue.text = "R: 0"; tvGValue.text = "G: 0"; tvBValue.text = "B: 0"
        colorPreview.setBackgroundColor(Color.BLACK)
    }
    
    private fun updateUI() {
        runOnUiThread {
            btnConnect.isEnabled = !isConnected && discoveredDevice != null
            btnDisconnect.isEnabled = isConnected
            btnScan.isEnabled = !isConnected
        }
    }
    
    private fun hasScanPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) hasPermission(Manifest.permission.BLUETOOTH_SCAN) else hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun hasConnectPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) hasPermission(Manifest.permission.BLUETOOTH_CONNECT) else true
    private fun showToast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}
