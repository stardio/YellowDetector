# ESP32 BLE MAC 주소 확인 방법

ESP32 보드를 교체한 경우, 새로운 BLE MAC 주소를 확인하고 Android 앱 코드를 업데이트해야 합니다.

## 방법 1: ESP32 시리얼 모니터로 확인 (가장 정확)

### 1.1 ESP32 코드 수정
ESP32 Arduino 코드에 다음 코드를 추가하세요:

```cpp
void setup() {
    Serial.begin(115200);
    
    // BLE 초기화 전에 MAC 주소 출력
    BLEDevice::init("ESP32_RGB_Sensor");
    
    // BLE MAC 주소 출력
    Serial.print("BLE MAC 주소: ");
    Serial.println(BLEDevice::getAddress().toString().c_str());
    
    // 나머지 코드...
}
```

### 1.2 시리얼 모니터 확인
1. Arduino IDE에서 `도구` > `시리얼 모니터` 열기
2. 보드 레이트: `115200` 설정
3. ESP32 보드 리셋 또는 업로드
4. 시리얼 모니터에 다음과 같은 출력 확인:
   ```
   BLE MAC 주소: XX:XX:XX:XX:XX:XX
   ```

## 방법 2: nRF Connect 앱으로 확인 (가장 간단)

### 2.1 nRF Connect 앱 설치
- Google Play Store에서 "nRF Connect" 앱 설치

### 2.2 스캔 및 확인
1. nRF Connect 앱 실행
2. `SCAN` 버튼 클릭
3. `ESP32_RGB_Sensor` 디바이스 찾기
4. 디바이스를 탭하여 상세 정보 확인
5. **MAC 주소** 확인 (예: `98:A3:16:E3:4D:1D` 형식)

## 방법 3: Android 앱 Logcat으로 확인 (개발자용)

### 3.1 MAC 주소 필터 제거 (임시)
Android 앱 코드에서 MAC 주소 필터를 일시적으로 제거하여 모든 디바이스를 로그에 출력합니다.

### 3.2 Logcat 확인
1. Android Studio에서 `Logcat` 탭 열기
2. 필터: `RGBSensorBLE`
3. "스캔 시작" 버튼 클릭
4. 로그에서 발견된 모든 디바이스의 MAC 주소 확인:
   ```
   발견된 디바이스: 이름=ESP32_RGB_Sensor, 주소=XX:XX:XX:XX:XX:XX
   ```

## 방법 4: Android 앱 UI에서 확인 (코드 수정 필요)

Android 앱을 수정하여 발견된 모든 디바이스의 MAC 주소를 화면에 표시할 수 있습니다.

---

## 새로운 MAC 주소로 Android 앱 업데이트

### 1. MainActivity.kt 파일 열기
경로: `app/src/main/java/com/example/rgbsensorble/MainActivity.kt`

### 2. MAC 주소 상수 찾기
약 48번째 줄 근처:
```kotlin
// ESP32 MAC 주소 (nRF Connect에서 확인)
private const val ESP32_MAC_ADDRESS = "98:A3:16:E3:4D:1D"
```

### 3. 새로운 MAC 주소로 변경
```kotlin
// ESP32 MAC 주소 (nRF Connect에서 확인)
private const val ESP32_MAC_ADDRESS = "새로운_MAC_주소"
```

**예시:**
```kotlin
private const val ESP32_MAC_ADDRESS = "A1:B2:C3:D4:E5:F6"
```

### 4. 저장 및 빌드
1. 파일 저장
2. Android Studio에서 `Build` > `Rebuild Project`
3. 앱 재실행

---

## MAC 주소 형식

ESP32 BLE MAC 주소는 다음과 같은 형식입니다:
- **형식**: `XX:XX:XX:XX:XX:XX` (대문자 또는 소문자)
- **예시**: `98:A3:16:E3:4D:1D` 또는 `98:a3:16:e3:4d:1d`
- **길이**: 17자 (콜론 포함)

**주의사항:**
- 대소문자는 구분하지 않습니다 (코드에서 `ignoreCase = true` 사용)
- 콜론(`:`)은 반드시 포함해야 합니다
- 공백이 있으면 안 됩니다

---

## 빠른 확인 체크리스트

- [ ] ESP32 보드 전원 켜기
- [ ] ESP32 코드 업로드 완료
- [ ] BLE 광고 시작 확인 (시리얼 모니터 또는 nRF Connect)
- [ ] MAC 주소 확인 (위 방법 중 하나 사용)
- [ ] Android 앱의 `MainActivity.kt`에서 MAC 주소 업데이트
- [ ] 앱 재빌드 및 테스트

---

## 문제 해결

### MAC 주소를 찾을 수 없음
1. ESP32 보드가 전원이 켜져 있는지 확인
2. BLE 코드가 정상적으로 업로드되었는지 확인
3. ESP32가 BLE 광고를 시작했는지 확인 (시리얼 모니터)
4. 스마트폰의 블루투스가 켜져 있는지 확인
5. 스마트폰과 ESP32가 가까운 거리에 있는지 확인

### MAC 주소가 계속 변경됨
- ESP32의 BLE MAC 주소는 보드마다 고유합니다
- 보드를 교체하면 MAC 주소가 변경됩니다
- 각 보드마다 Android 앱의 MAC 주소를 업데이트해야 합니다

### 여러 ESP32 보드 사용
- 각 보드마다 다른 MAC 주소를 가집니다
- Android 앱에서 여러 MAC 주소를 지원하려면 코드 수정이 필요합니다
- 또는 디바이스 이름(`ESP32_RGB_Sensor`)으로 필터링할 수도 있습니다

---

## 참고: ESP32 BLE MAC 주소 특징

1. **고유성**: 각 ESP32 보드는 고유한 MAC 주소를 가집니다
2. **불변성**: 한 보드의 MAC 주소는 변경되지 않습니다
3. **형식**: 6바이트 주소를 콜론으로 구분하여 표시
4. **공개 주소**: ESP32는 공개(Public) BLE 주소를 사용합니다




