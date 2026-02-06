#include <Wire.h>
#include "DFRobot_TCS34725.h"
#include <ModbusRTU.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// 핀 설정
#define SDA_PIN 12 
#define SCL_PIN 33
#define RS485_RE_PIN 17       
#define RS485_SHUTDOWN_PIN 19 
#define BOOST_ENABLE_PIN 16   

// BLE UUID 설정
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

ModbusRTU mb;
DFRobot_TCS34725 tcs = DFRobot_TCS34725(&Wire, TCS34725_ADDRESS, TCS34725_INTEGRATIONTIME_50MS, TCS34725_GAIN_4X);
BLECharacteristic *pCharacteristic;
bool deviceConnected = false;

uint16_t RAW_R, RAW_G, RAW_B, RAW_C;

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) { deviceConnected = true; };
    void onDisconnect(BLEServer* pServer) { 
        deviceConnected = false;
        BLEDevice::startAdvertising(); // 연결 끊기면 다시 찾기 가능하게
    }
};

void setup() {
    Serial.begin(115200);
    pinMode(BOOST_ENABLE_PIN, OUTPUT); digitalWrite(BOOST_ENABLE_PIN, HIGH);
    pinMode(RS485_SHUTDOWN_PIN, OUTPUT); digitalWrite(RS485_SHUTDOWN_PIN, HIGH);

    // I2C & Sensor
    Wire.begin(SDA_PIN, SCL_PIN);
    tcs.begin();

    // Modbus
    Serial1.begin(115200, SERIAL_8N1, 21, 22);
    mb.begin(&Serial1, RS485_RE_PIN);
    mb.slave(1);
    mb.addIreg(257); mb.addIreg(258); mb.addIreg(259);

    // BLE Init
    BLEDevice::init("ESP32_Yellow_Sensor");
    BLEServer *pServer = BLEDevice::createServer();
    pServer->setCallbacks(new MyServerCallbacks());
    BLEService *pService = pServer->createService(SERVICE_UUID);
    pCharacteristic = pService->createCharacteristic(CHARACTERISTIC_UUID, BLECharacteristic::PROPERTY_NOTIFY);
    pCharacteristic->addDescriptor(new BLE2902());
    pService->start();
    BLEDevice::startAdvertising();

    Serial.println("PLC + BLE System Ready!");
}

void loop() {
    mb.task();
    
    static unsigned long lastTime = 0;
    if (millis() - lastTime > 200) {
        tcs.getRGBC(&RAW_R, &RAW_G, &RAW_B, &RAW_C);
        
        // PLC 전송
        mb.Ireg(257, RAW_B); mb.Ireg(258, RAW_R); mb.Ireg(259, RAW_G);

        // BLE Notify (8바이트 패킷 전송)
        if (deviceConnected) {
            uint8_t data[8] = { (uint8_t)(RAW_R >> 8), (uint8_t)RAW_R, (uint8_t)(RAW_G >> 8), (uint8_t)RAW_G,
                                (uint8_t)(RAW_B >> 8), (uint8_t)RAW_B, (uint8_t)(RAW_C >> 8), (uint8_t)RAW_C };
            pCharacteristic->setValue(data, 8);
            pCharacteristic->notify();
        }
        lastTime = millis();
    }
    yield();
}