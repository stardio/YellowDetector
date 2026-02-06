# APK 빌드 및 스마트폰 설치 가이드

## 방법 1: Android Studio에서 직접 빌드 (가장 간단)

### 1단계: 디버그 APK 빌드

1. **Android Studio에서 프로젝트 열기**
   - `C:\Users\BSA\AndroidStudioProjects\yellow` 폴더 열기

2. **APK 빌드**
   - 상단 메뉴: **Build > Build Bundle(s) / APK(s) > Build APK(s)**
   - 빌드 완료까지 대기 (하단에 진행 상황 표시)

3. **APK 파일 위치 확인**
   - 빌드 완료 후 하단에 알림 표시
   - **locate** 링크 클릭하거나
   - 직접 경로: `app\build\outputs\apk\debug\app-debug.apk`

### 2단계: 스마트폰에 설치

#### 방법 A: USB 케이블 연결 (권장)

1. **스마트폰 준비**
   - 스마트폰에서 **설정 > 개발자 옵션** 활성화
   - **USB 디버깅** 활성화
   - USB 케이블로 PC와 연결
   - "USB 디버깅 허용" 확인

2. **Android Studio에서 직접 설치**
   - 상단 툴바에서 **Run** 버튼 클릭 (녹색 재생 아이콘)
   - 또는 **Run > Run 'app'** (단축키: `Shift+F10`)
   - 연결된 기기 선택
   - 앱이 자동으로 빌드되고 설치됨

#### 방법 B: APK 파일 직접 전송

1. **APK 파일 복사**
   - `app\build\outputs\apk\debug\app-debug.apk` 파일 복사
   - 스마트폰으로 전송 (이메일, USB, 클라우드 등)

2. **스마트폰에서 설치**
   - 스마트폰에서 **설정 > 보안 > 알 수 없는 소스** 허용
   - 또는 설치 시 "이 출처 허용" 선택
   - APK 파일 실행하여 설치

---

## 방법 2: 명령줄에서 빌드 (PowerShell)

### 1단계: Gradle Wrapper 확인

프로젝트에 `gradlew.bat` 파일이 있는지 확인합니다. 없으면 Android Studio에서 프로젝트를 한 번 열면 자동 생성됩니다.

### 2단계: APK 빌드

PowerShell에서 다음 명령 실행:

```powershell
cd C:\Users\BSA\AndroidStudioProjects\yellow

# 디버그 APK 빌드
.\gradlew.bat assembleDebug
```

### 3단계: APK 파일 위치

빌드된 APK 파일 위치:
```
app\build\outputs\apk\debug\app-debug.apk
```

### 4단계: 스마트폰에 설치

#### USB로 직접 설치 (ADB 사용)

```powershell
# ADB로 기기 확인
adb devices

# APK 설치
adb install app\build\outputs\apk\debug\app-debug.apk
```

**참고:** ADB가 설치되어 있어야 합니다 (Android SDK의 platform-tools에 포함).

---

## 방법 3: 릴리즈 APK 빌드 (배포용)

### 1단계: 서명 키 생성 (처음 한 번만)

1. **Android Studio에서**
   - **Build > Generate Signed Bundle / APK** 선택
   - **APK** 선택 후 **Next**

2. **키스토어 생성**
   - **Create new...** 클릭
   - 키스토어 정보 입력:
     - Key store path: 키스토어 파일 저장 위치
     - Password: 비밀번호 입력
     - Key alias: 키 별칭
     - Key password: 키 비밀번호
     - Validity: 유효 기간 (예: 25년)
     - Certificate 정보 입력
   - **OK** 클릭

### 2단계: 릴리즈 APK 빌드

1. **기존 키스토어 사용**
   - **Choose existing...** 클릭하여 키스토어 선택
   - 비밀번호 입력

2. **빌드 타입 선택**
   - **release** 선택
   - **Finish** 클릭

3. **APK 파일 위치**
   - `app\build\outputs\apk\release\app-release.apk`

---

## 빠른 설치 방법 (가장 간단)

### Android Studio 사용 (추천)

1. 스마트폰 USB 연결
2. Android Studio에서 **Run** 버튼 클릭
3. 끝! (자동으로 빌드하고 설치함)

### APK 파일 직접 전송

1. Android Studio에서 **Build > Build Bundle(s) / APK(s) > Build APK(s)**
2. 빌드 완료 후 `app-debug.apk` 파일을 스마트폰으로 전송
3. 스마트폰에서 파일 실행하여 설치

---

## 문제 해결

### "알 수 없는 소스" 오류
- **설정 > 보안 > 알 수 없는 소스** 허용
- 또는 설치 시 "이 출처 허용" 선택

### "USB 디버깅 허용" 팝업이 안 뜸
- USB 케이블 확인
- USB 드라이버 설치 확인
- 다른 USB 포트 시도

### ADB 명령어를 찾을 수 없음
- Android SDK의 platform-tools 경로를 PATH에 추가
- 또는 Android Studio의 내장 터미널 사용

### 빌드 실패
- **Build > Clean Project** 실행
- **Build > Rebuild Project** 실행
- Gradle 동기화 확인

---

## APK 파일 크기

- **디버그 APK**: 약 5-10MB (테스트용)
- **릴리즈 APK**: 약 3-5MB (배포용, 최적화됨)

---

## 다음 단계

APK가 설치되면:
1. 스마트폰에서 앱 실행
2. ESP32-S3 기기 전원 켜기
3. "스캔 시작" 버튼 클릭
4. "ESP32_RGB_Sensor" 디바이스 발견 후 연결
5. 실시간 RGB 값 확인!




