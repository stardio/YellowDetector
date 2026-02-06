# APK 직접 배포 가이드

다른 사람의 휴대폰에 앱을 직접 설치하는 방법입니다.

## 방법 1: 디버그 APK 빌드 (간단, 테스트용)

### 1.1 Android Studio에서 빌드
1. Android Studio에서 프로젝트 열기
2. `Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)`
3. 빌드 완료 후 `locate` 클릭
4. 생성된 파일: `app/build/outputs/apk/debug/app-debug.apk`

### 1.2 명령어로 빌드
프로젝트 루트 디렉토리에서:
```bash
./gradlew assembleDebug
```
또는 Windows PowerShell:
```powershell
.\gradlew.bat assembleDebug
```

생성 위치: `app\build\outputs\apk\debug\app-debug.apk`

## 방법 2: 릴리즈 APK 빌드 (권장, 배포용)

### 2.1 키스토어 생성 (최초 1회만)
```bash
keytool -genkey -v -keystore rgbsensor-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias rgbsensor
```

**필요한 정보:**
- 키스토어 비밀번호 (기억해야 함!)
- 키 별칭: rgbsensor
- 키 별칭 비밀번호
- 이름, 조직 등

### 2.2 build.gradle 설정
`app/build.gradle` 파일에 다음 추가:

```gradle
android {
    ...
    
    signingConfigs {
        release {
            storeFile file('rgbsensor-release-key.jks')
            storePassword '비밀번호'
            keyAlias 'rgbsensor'
            keyPassword '비밀번호'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
        }
    }
}
```

### 2.3 릴리즈 APK 빌드
```bash
./gradlew assembleRelease
```

생성 위치: `app\build\outputs\apk\release\app-release.apk`

## 방법 3: 자동화 스크립트 사용

프로젝트 루트에 `APK_빌드.ps1` 스크립트가 있습니다. 이를 실행하면 자동으로 APK를 빌드합니다.

## 4. APK 파일 전송 방법

### 4.1 이메일로 전송
1. APK 파일을 이메일에 첨부
2. 받는 사람이 이메일에서 다운로드
3. 다운로드한 파일을 탭하여 설치

### 4.2 USB 케이블로 전송
1. APK 파일을 컴퓨터에 복사
2. USB 케이블로 휴대폰 연결
3. 파일 탐색기에서 APK 파일을 휴대폰으로 복사
4. 휴대폰에서 파일 관리자로 APK 파일 찾아서 설치

### 4.3 클라우드 저장소 사용
- Google Drive
- Dropbox
- OneDrive
- 네이버 클라우드
등에 업로드하고 링크 공유

### 4.4 메신저로 전송
- 카카오톡
- 텔레그램
- WhatsApp
등으로 APK 파일 전송

### 4.5 QR 코드 생성
1. APK 파일을 클라우드에 업로드
2. 다운로드 링크를 QR 코드로 생성
3. QR 코드를 스캔하여 다운로드

## 5. 다른 사람의 휴대폰에 설치하는 방법

### 5.1 알 수 없는 출처 허용 (필수)
Android는 보안상 알 수 없는 출처(Unknown Sources)에서 앱 설치를 기본적으로 차단합니다.

**설정 방법:**
1. `설정` > `보안` (또는 `앱 및 알림` > `특수 앱 액세스`)
2. `알 수 없는 출처` 또는 `알 수 없는 앱 설치` 활성화
3. 또는 APK 파일을 열 때 "이 출처 허용" 선택

**Android 버전별 경로:**
- Android 8.0 이상: 각 앱별로 허용 (파일 관리자 앱에서 허용)
- Android 7.0 이하: 전체적으로 허용

### 5.2 APK 파일 설치
1. APK 파일을 휴대폰에 다운로드/복사
2. 파일 관리자 앱에서 APK 파일 찾기
3. APK 파일 탭
4. "설치" 버튼 클릭
5. 권한 요청 시 "설치" 확인
6. 설치 완료 후 "열기" 또는 홈 화면에서 앱 실행

## 6. 주의사항

### 6.1 보안 경고
- Android는 Google Play Store 외부에서 설치한 앱에 대해 경고를 표시합니다.
- 이는 정상적인 현상입니다.
- 신뢰할 수 있는 출처에서만 APK를 설치하도록 안내하세요.

### 6.2 버전 관리
- 여러 사람에게 배포할 경우 버전 번호를 관리하세요.
- `app/build.gradle`의 `versionCode`와 `versionName`을 업데이트하세요.

### 6.3 업데이트
- 새 버전 배포 시 `versionCode`를 증가시켜야 합니다.
- 사용자가 새 APK를 설치하면 자동으로 업데이트됩니다.

## 7. 빠른 빌드 스크립트

프로젝트에 `APK_빌드.ps1` 스크립트가 있습니다. 이를 실행하면:
1. 디버그 APK 자동 빌드
2. 빌드된 APK 파일 위치 표시
3. 파일 탐색기에서 열기

**사용 방법:**
```powershell
.\APK_빌드.ps1
```

## 8. APK 파일 크기 최적화 (선택사항)

### 8.1 ProGuard 활성화
`app/build.gradle`에서:
```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

### 8.2 불필요한 리소스 제거
- 사용하지 않는 이미지, 레이아웃 파일 제거
- 다국어 지원이 필요 없으면 특정 언어만 유지

## 9. 배포 체크리스트

- [ ] APK 파일 빌드 완료
- [ ] 앱이 정상 작동하는지 테스트
- [ ] APK 파일을 안전한 방법으로 전송
- [ ] 설치 방법 안내 문서 제공
- [ ] "알 수 없는 출처" 허용 방법 안내
- [ ] 버전 정보 확인

## 10. 문제 해결

### 10.1 "앱이 설치되지 않음"
- 저장 공간 확인
- 이전 버전 완전 삭제 후 재설치
- "알 수 없는 출처" 허용 확인

### 10.2 "패키지가 손상됨"
- APK 파일을 다시 빌드
- 전송 중 파일이 손상되었을 수 있음

### 10.3 "앱이 실행되지 않음"
- 권한 확인 (BLE, 위치 권한)
- Android 버전 호환성 확인 (최소 SDK 버전)

---

**팁:** 여러 사람에게 배포할 경우, 간단한 설치 가이드와 함께 APK를 제공하면 좋습니다.




