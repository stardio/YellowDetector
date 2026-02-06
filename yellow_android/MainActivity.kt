package com.example.rgbsensorble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "RGBSensorBLE"
        
        // ESP32 BLE UUID (Arduino 코드와 동일)
        private val SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        private val CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        private val DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        
        // ESP32 디바이스 이름
        private const val DEVICE_NAME = "ESP32_RGB_Sensor"
        
        // 권한 요청 코드
        private const val REQUEST_PERMISSIONS = 100
        
        // 스캔 타임아웃 (10초)
        private const val SCAN_PERIOD: Long = 10000
    }
    
    // BLE 관련 변수
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null
    
    // UI 요소
    private lateinit var btnScan: Button
    private lateinit var btnConnect: Button
    private lateinit var btnDisconnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvRValue: TextView
    private lateinit var tvGValue: TextView
    private lateinit var tvBValue: TextView
    private lateinit var colorPreview: View
    
    // 상태 변수
    private var isScanning = false
    private var isConnected = false
    private var discoveredDevice: BluetoothDevice? = null
    private var deviceAddress: String? = null
    
    // Handler
    private val handler = Handler(Looper.getMainLooper())
    
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
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null) {
            showToast("이 기기는 BLE를 지원하지 않습니다")
            finish()
            return
        }
        
        if (!bluetoothAdapter.isEnabled) {
            showToast("블루투스를 활성화해주세요")
        }
        
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    }
    
    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 이상
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            // Android 11 이하
            if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
        
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }
    
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == REQUEST_PERMISSIONS) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                showToast("권한이 필요합니다")
            }
        }
    }
    
    private fun startScan() {
        if (!hasAllPermissions()) {
            checkPermissions()
            return
        }
        
        if (isScanning) {
            stopScan()
            return
        }
        
        isScanning = true
        btnScan.text = "스캔 중지"
        tvStatus.text = "스캔 중..."
        discoveredDevice = null
        deviceAddress = null
        
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        
        handler.postDelayed({
            if (isScanning) {
                stopScan()
            }
        }, SCAN_PERIOD)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothLeScanner.startScan(null, scanSettings, scanCallback)
        } else {
            @Suppress("DEPRECATION")
            bluetoothLeScanner.startScan(scanCallback)
        }
        
        Log.d(TAG, "BLE 스캔 시작")
    }
    
    private fun stopScan() {
        if (!isScanning) return
        
        isScanning = false
        btnScan.text = "스캔 시작"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothLeScanner.stopScan(scanCallback)
        } else {
            @Suppress("DEPRECATION")
            bluetoothLeScanner.stopScan(scanCallback)
        }
        
        if (discoveredDevice == null) {
            tvStatus.text = "디바이스를 찾을 수 없습니다"
        }
        
        Log.d(TAG, "BLE 스캔 중지")
    }
    
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            
            val device = result.device
            val deviceName = device.name ?: "Unknown"
            
            Log.d(TAG, "발견된 디바이스: $deviceName, 주소: ${device.address}")
            
            if (deviceName == DEVICE_NAME || device.address == deviceAddress) {
                discoveredDevice = device
                deviceAddress = device.address
                stopScan()
                
                runOnUiThread {
                    tvStatus.text = "디바이스 발견: $deviceName"
                    showToast("ESP32-S3 발견!")
                }
            }
        }
        
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e(TAG, "스캔 실패: $errorCode")
            
            runOnUiThread {
                tvStatus.text = "스캔 실패: $errorCode"
                isScanning = false
                btnScan.text = "스캔 시작"
            }
        }
    }
    
    private fun connectToDevice() {
        if (discoveredDevice == null) {
            showToast("먼저 디바이스를 스캔하세요")
            return
        }
        
        if (!hasAllPermissions()) {
            checkPermissions()
            return
        }
        
        tvStatus.text = "연결 중..."
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothGatt = discoveredDevice?.connectGatt(
                this,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
        } else {
            @Suppress("DEPRECATION")
            bluetoothGatt = discoveredDevice?.connectGatt(this, false, gattCallback)
        }
        
        Log.d(TAG, "디바이스 연결 시도: ${discoveredDevice?.address}")
    }
    
    private fun disconnectDevice() {
        bluetoothGatt?.let { gatt ->
            if (hasAllPermissions()) {
                gatt.disconnect()
                gatt.close()
            }
        }
        bluetoothGatt = null
        isConnected = false
        discoveredDevice = null
        deviceAddress = null
        
        runOnUiThread {
            tvStatus.text = "연결 해제됨"
            updateUI()
            resetRGBValues()
        }
        
        Log.d(TAG, "디바이스 연결 해제")
    }
    
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "연결 성공")
                    runOnUiThread {
                        tvStatus.text = "연결됨"
                        isConnected = true
                        updateUI()
                    }
                    
                    // 서비스 발견 시작
                    handler.postDelayed({
                        if (hasAllPermissions()) {
                            gatt.discoverServices()
                        }
                    }, 500)
                }
                
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "연결 해제됨")
                    runOnUiThread {
                        isConnected = false
                        updateUI()
                        resetRGBValues()
                    }
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "서비스 발견 성공")
                
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                    if (characteristic != null) {
                        enableNotification(gatt, characteristic)
                    } else {
                        Log.e(TAG, "특성을 찾을 수 없습니다")
                        runOnUiThread {
                            tvStatus.text = "특성을 찾을 수 없습니다"
                        }
                    }
                } else {
                    Log.e(TAG, "서비스를 찾을 수 없습니다")
                    runOnUiThread {
                        tvStatus.text = "서비스를 찾을 수 없습니다"
                    }
                }
            } else {
                Log.e(TAG, "서비스 발견 실패: $status")
            }
        }
        
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            
            val data = characteristic.value
            val dataString = String(data, Charsets.UTF_8)
            
            Log.d(TAG, "수신된 데이터: $dataString")
            
            parseRGBData(dataString)
        }
    }
    
    private fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (!hasAllPermissions()) {
            Log.e(TAG, "권한이 없습니다")
            return
        }
        
        // Notification 활성화
        gatt.setCharacteristicNotification(characteristic, true)
        
        // CCCD 설정
        val descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID)
        descriptor?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(it)
            Log.d(TAG, "Notification 활성화 완료")
        }
    }
    
    private fun parseRGBData(data: String) {
        try {
            // "R255,G255,B255" 형식 파싱
            val trimmed = data.trim()
            val parts = trimmed.split(",")
            
            if (parts.size == 3) {
                val r = parts[0].substring(1).toInt().coerceIn(0, 255)
                val g = parts[1].substring(1).toInt().coerceIn(0, 255)
                val b = parts[2].substring(1).toInt().coerceIn(0, 255)
                
                runOnUiThread {
                    tvRValue.text = "R: $r"
                    tvGValue.text = "G: $g"
                    tvBValue.text = "B: $b"
                    
                    // 색상 미리보기 업데이트
                    val color = android.graphics.Color.rgb(r, g, b)
                    colorPreview.setBackgroundColor(color)
                }
                
                Log.d(TAG, "RGB 값 업데이트: R=$r, G=$g, B=$b")
            } else {
                Log.w(TAG, "잘못된 데이터 형식: $data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "데이터 파싱 오류: ${e.message}", e)
        }
    }
    
    private fun resetRGBValues() {
        tvRValue.text = "R: 0"
        tvGValue.text = "G: 0"
        tvBValue.text = "B: 0"
        colorPreview.setBackgroundColor(android.graphics.Color.BLACK)
    }
    
    private fun updateUI() {
        btnConnect.isEnabled = !isConnected && discoveredDevice != null
        btnDisconnect.isEnabled = isConnected
        btnScan.isEnabled = !isConnected && !isScanning
    }
    
    private fun hasAllPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
            hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopScan()
        disconnectDevice()
    }
}





