# 안드로이드 BLE RGB 센서 앱

ESP32-S3에서 전송하는 RGB 센서 데이터를 수신하는 안드로이드 앱입니다.

## 프로젝트 설정 방법

### 1. Android Studio에서 프로젝트 생성

1. Android Studio 실행
2. **File > New > New Project**
3. **Empty Activity** 템플릿 선택
4. 프로젝트 정보 입력:
   - Name: `RGBSensorBLE`
   - Package name: `com.example.rgbsensorble`
   - Language: **Kotlin**
   - Minimum SDK: **API 21** (Android 5.0)

### 2. 파일 복사

생성된 프로젝트에 다음 파일들을 복사하세요:

- `MainActivity.kt` → `app/src/main/java/com/example/rgbsensorble/MainActivity.kt`
- `activity_main.xml` → `app/src/main/res/layout/activity_main.xml`
- `AndroidManifest.xml` → `app/src/main/AndroidManifest.xml` (기존 내용 교체)
- `build.gradle` → `app/build.gradle` (dependencies 부분 확인)

### 3. Gradle 동기화

Android Studio에서 **File > Sync Project with Gradle Files** 실행

### 4. 권한 확인

`AndroidManifest.xml`에 모든 권한이 포함되어 있는지 확인하세요.

## 사용 방법

1. **ESP32-S3 전원 켜기**
   - Arduino 코드 업로드 확인
   - 시리얼 모니터에서 "BLE 디바이스 이름: ESP32_RGB_Sensor" 메시지 확인

2. **앱 실행**
   - 안드로이드 기기에서 앱 실행
   - 권한 요청 시 허용

3. **BLE 스캔**
   - "스캔 시작" 버튼 클릭
   - "ESP32_RGB_Sensor" 디바이스 발견 대기

4. **연결**
   - "연결" 버튼 클릭
   - 연결 성공 시 RGB 값이 실시간으로 표시됨

5. **데이터 확인**
   - R, G, B 값이 화면에 표시됨
   - 색상 미리보기 영역에 실제 색상 표시

## 주요 기능

- ✅ BLE 디바이스 스캔
- ✅ ESP32-S3 자동 감지
- ✅ BLE 연결 및 해제
- ✅ 실시간 RGB 데이터 수신
- ✅ 색상 미리보기
- ✅ Android 12+ 권한 지원

## 문제 해결

### 디바이스를 찾을 수 없음
- ESP32-S3가 전원이 켜져 있는지 확인
- 블루투스가 활성화되어 있는지 확인
- 앱 권한이 허용되었는지 확인

### 연결 실패
- ESP32-S3와 안드로이드 기기가 가까운 거리에 있는지 확인
- ESP32-S3의 BLE가 정상 작동하는지 시리얼 모니터로 확인

### 데이터가 수신되지 않음
- Notification이 활성화되었는지 확인
- ESP32-S3에서 데이터가 전송되고 있는지 확인

## 참고사항

- 최소 Android 버전: 5.0 (API 21)
- BLE 지원 기기 필요
- 위치 권한 필요 (BLE 스캔용)


