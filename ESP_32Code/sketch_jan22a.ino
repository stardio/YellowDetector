#include <Wire.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <ModbusRTU.h>

// ------------------ 핀 및 설정 ------------------
#define SDA_PIN 41
#define SCL_PIN 42
#define RS485_RX 18 
#define RS485_TX 17
#define SLAVE_ID 1

// TCS34725 레지스터 정의
#define RGB_SENSOR_ADDRESS 0x29
#define TCS34725_COMMAND_BIT  0x80
#define TCS34725_ENABLE       0x00
#define TCS34725_ATIME        0x01
#define TCS34725_CONTROL       0x0F
#define TCS34725_ID            0x12
#define TCS34725_CDATAL       0x14
#define TCS34725_ENABLE_AEN   0x02
#define TCS34725_ENABLE_PON   0x01

// ------------------ 전역 변수 ------------------
ModbusRTU mb;
BLEServer* pServer = nullptr;
BLECharacteristic* pCharacteristic = nullptr;
bool deviceConnected = false;

uint16_t RAW_C = 0, RAW_R = 0, RAW_G = 0, RAW_B = 0;
unsigned long lastSensorRead = 0;
const unsigned long sensorInterval = 100; // 100ms 주기로 데이터 업데이트

// ------------------ 센서 제어 함수 ------------------
void tcsWrite8(uint8_t reg, uint8_t value) {
  Wire.beginTransmission(RGB_SENSOR_ADDRESS);
  Wire.write(TCS34725_COMMAND_BIT | reg);
  Wire.write(value);
  Wire.endTransmission();
}

uint8_t tcsRead8(uint8_t reg) {
  Wire.beginTransmission(RGB_SENSOR_ADDRESS);
  Wire.write(TCS34725_COMMAND_BIT | reg);
  Wire.endTransmission();
  Wire.requestFrom(RGB_SENSOR_ADDRESS, (uint8_t)1);
  return Wire.available() ? Wire.read() : 0;
}

bool initRGBSensor() {
  uint8_t id = tcsRead8(TCS34725_ID);
  if (id != 0x44 && id != 0x4D && id != 0x10) return false;
  tcsWrite8(TCS34725_ATIME, 0xEB); // 응답성 향상을 위한 짧은 적분 시간
  tcsWrite8(TCS34725_CONTROL, 0x01); // 4x Gain
  tcsWrite8(TCS34725_ENABLE, TCS34725_ENABLE_PON);
  delay(3);
  tcsWrite8(TCS34725_ENABLE, TCS34725_ENABLE_PON | TCS34725_ENABLE_AEN);
  return true;
}

void readRGBSensor() {
  Wire.beginTransmission(RGB_SENSOR_ADDRESS);
  Wire.write(TCS34725_COMMAND_BIT | TCS34725_CDATAL);
  Wire.endTransmission();
  Wire.requestFrom(RGB_SENSOR_ADDRESS, (uint8_t)8);
  if (Wire.available() == 8) {
    RAW_C = Wire.read() | (Wire.read() << 8);
    RAW_R = Wire.read() | (Wire.read() << 8);
    RAW_G = Wire.read() | (Wire.read() << 8);
    RAW_B = Wire.read() | (Wire.read() << 8);
  }
}

// ------------------ BLE 콜백 ------------------
class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) override { deviceConnected = true; }
  void onDisconnect(BLEServer* pServer) override { deviceConnected = false; }
};

// ------------------ SETUP ------------------
void setup() {
  Serial.begin(115200);
  
  // 1. 성공하신 115200bps로 Modbus 설정
  Serial1.begin(115200, SERIAL_8N1, RS485_RX, RS485_TX);
  mb.begin(&Serial1);
  mb.slave(SLAVE_ID);
  
  // PLC 연속 주소 할당 (D100, D101, D102 매칭)
  mb.addIreg(257); // Blue (0x30101)
  mb.addIreg(258); // Red  (0x30102)
  mb.addIreg(259); // Green(0x30103)

  // 2. I2C 센서 초기화
  Wire.begin(SDA_PIN, SCL_PIN);
  Wire.setClock(400000); 
  if(!initRGBSensor()) Serial.println("Sensor Init Failed!");

  // 3. BLE 설정
  BLEDevice::init("ESP32_RGB_Sensor");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  BLEService* pService = pServer->createService("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
  pCharacteristic = pService->createCharacteristic("beb5483e-36e1-4688-b7f5-ea07361b26a8", 
                    BLECharacteristic::PROPERTY_NOTIFY);
  pCharacteristic->addDescriptor(new BLE2902());
  pService->start();
  BLEDevice::startAdvertising();

  Serial.println("System Ready: RGB to PLC (D100-D102) @ 115200bps");
}

// ------------------ LOOP ------------------
void loop() {
  // PLC의 Modbus 요청을 최우선으로 처리
  mb.task(); 

  unsigned long now = millis();
  
  // 100ms 주기로 데이터 업데이트
  if (now - lastSensorRead >= sensorInterval) {
    lastSensorRead = now;
    
    readRGBSensor(); // 센서값 읽기

    // PLC 레지스터에 RGB 값 기록 (D100-D102)
    mb.Ireg(257, RAW_B); 
    mb.Ireg(258, RAW_R);
    mb.Ireg(259, RAW_G);

    // 디버깅용 시리얼 출력
    Serial.printf("B:%u R:%u G:%u\n", RAW_B, RAW_R, RAW_G);

    // BLE 전송 (연결된 경우에만)
    if (deviceConnected) {
      uint8_t buf[8] = { (uint8_t)RAW_C, (uint8_t)(RAW_C >> 8), (uint8_t)RAW_R, (uint8_t)(RAW_R >> 8),
                         (uint8_t)RAW_G, (uint8_t)(RAW_G >> 8), (uint8_t)RAW_B, (uint8_t)(RAW_B >> 8) };
      pCharacteristic->setValue(buf, 8);
      pCharacteristic->notify();
    }
  }

  // BLE 광고 상태 관리
  static bool oldConnected = false;
  if (!deviceConnected && oldConnected) {
    delay(500);
    pServer->startAdvertising();
    oldConnected = deviceConnected;
  }
  if (deviceConnected && !oldConnected) oldConnected = deviceConnected;

  yield(); // 시스템 안정화
}