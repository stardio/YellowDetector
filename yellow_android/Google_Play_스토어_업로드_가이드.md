# Google Play Store 앱 업로드 가이드

## 1. 사전 준비 사항

### 1.1 Google Play Console 계정 생성
- Google Play Console (https://play.google.com/console) 접속
- Google 계정으로 로그인
- 개발자 등록비: **$25 (일회성, 평생 유효)**

### 1.2 앱 서명 키 생성
앱을 서명하기 위한 키스토어 파일을 생성해야 합니다.

**명령어 (PowerShell 또는 명령 프롬프트):**
```bash
keytool -genkey -v -keystore rgbsensor-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias rgbsensor
```

**필요한 정보:**
- 키스토어 비밀번호
- 키 별칭 비밀번호
- 이름, 조직 단위, 조직, 도시, 시/도, 국가 코드

**중요:** 키스토어 파일과 비밀번호를 안전하게 보관하세요! 분실하면 앱 업데이트가 불가능합니다.

## 2. 앱 빌드 설정

### 2.1 build.gradle (app) 수정
`app/build.gradle` 파일에 릴리즈 빌드 설정 추가:

```gradle
android {
    ...
    
    signingConfigs {
        release {
            storeFile file('경로/rgbsensor-release-key.jks')
            storePassword '키스토어비밀번호'
            keyAlias 'rgbsensor'
            keyPassword '키별칭비밀번호'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

**보안 주의:** 비밀번호를 직접 코드에 넣지 마세요! `gradle.properties` 파일에 저장하고 참조하세요.

### 2.2 gradle.properties에 키 정보 저장 (선택사항)
```properties
RELEASE_STORE_FILE=rgbsensor-release-key.jks
RELEASE_STORE_PASSWORD=키스토어비밀번호
RELEASE_KEY_ALIAS=rgbsensor
RELEASE_KEY_PASSWORD=키별칭비밀번호
```

그리고 `build.gradle`에서:
```gradle
signingConfigs {
    release {
        storeFile file(RELEASE_STORE_FILE)
        storePassword RELEASE_STORE_PASSWORD
        keyAlias RELEASE_KEY_ALIAS
        keyPassword RELEASE_KEY_PASSWORD
    }
}
```

## 3. 릴리즈 APK/AAB 빌드

### 3.1 Android App Bundle (AAB) 빌드 (권장)
Google Play는 AAB 형식을 권장합니다.

**Android Studio에서:**
1. `Build` > `Generate Signed Bundle / APK`
2. `Android App Bundle` 선택
3. 키스토어 파일 선택 및 비밀번호 입력
4. `release` 빌드 타입 선택
5. 빌드 완료 후 `app/release/app-release.aab` 파일 생성됨

**명령어로 빌드:**
```bash
./gradlew bundleRelease
```
생성 위치: `app/build/outputs/bundle/release/app-release.aab`

### 3.2 APK 빌드 (대안)
```bash
./gradlew assembleRelease
```
생성 위치: `app/build/outputs/apk/release/app-release.apk`

## 4. Google Play Console 설정

### 4.1 새 앱 만들기
1. Google Play Console 접속
2. `앱 만들기` 클릭
3. 앱 정보 입력:
   - 앱 이름: "LED RGB 센서" (또는 원하는 이름)
   - 기본 언어: 한국어
   - 앱 또는 게임: 앱
   - 무료 또는 유료: 무료

### 4.2 앱 콘텐츠 등급 설정
- 앱 콘텐츠 등급 설문 작성
- 앱의 기능과 콘텐츠에 대한 질문에 답변
- 등급이 자동으로 결정됨

### 4.3 타겟 대상 및 콘텐츠 설정
- 타겟 연령대 선택
- 데이터 보안 섹션 작성
- 개인정보처리방침 URL (필요시)

## 5. 앱 정보 입력

### 5.1 스토어 등록정보
- **앱 이름**: LED RGB 센서
- **간단한 설명**: (80자 이내)
  예: "ESP32-S3와 BLE로 연결하여 RGB 센서 데이터를 실시간으로 확인하는 앱"
- **전체 설명**: (4000자 이내)
  - 앱의 주요 기능 설명
  - 사용 방법
  - 필요한 권한 설명
- **그래픽 자산**:
  - 앱 아이콘: 512x512px PNG (투명 배경 없음)
  - 기능 그래픽: 1024x500px (선택사항)
  - 스크린샷: 최소 2개, 최대 8개
    - 휴대전화: 최소 1080px 높이
    - 7인치 태블릿: 최소 1200px 높이
    - 10인치 태블릿: 최소 1600px 높이

### 5.2 분류
- 카테고리: 도구 또는 생산성
- 태그: 선택사항

## 6. 앱 업로드

### 6.1 프로덕션 트랙에 업로드
1. 좌측 메뉴에서 `프로덕션` 선택
2. `새 버전 만들기` 클릭
3. AAB 파일 업로드
4. 버전 이름 입력 (예: "1.0.0")
5. 릴리즈 노트 작성 (선택사항)

### 6.2 테스트 트랙 (선택사항)
- 내부 테스트: 최대 100명
- 비공개 테스트: 최대 1000명
- 공개 테스트: 제한 없음

## 7. 검토 제출

### 7.1 필수 항목 확인
- [ ] 앱 서명 완료
- [ ] 스토어 등록정보 완료
- [ ] 콘텐츠 등급 완료
- [ ] 타겟 대상 및 콘텐츠 완료
- [ ] 데이터 보안 섹션 완료
- [ ] 개인정보처리방침 (필요시)

### 7.2 검토 제출
1. 모든 필수 항목 완료 확인
2. `검토 제출` 버튼 클릭
3. 검토 완료까지 보통 **1-3일** 소요

## 8. 앱 출시 후

### 8.1 앱 모니터링
- 사용자 리뷰 확인
- 크래시 및 ANR 모니터링
- 앱 통계 확인

### 8.2 업데이트
- 새 버전 빌드
- 버전 코드 증가 (`versionCode`)
- 버전 이름 업데이트 (`versionName`)
- 프로덕션 트랙에 새 버전 업로드

## 9. 중요 참고사항

### 9.1 권한 설명
BLE 관련 권한 사용 시 스토어 등록정보에 명확히 설명해야 합니다:
- "이 앱은 ESP32-S3 디바이스와 블루투스로 연결하기 위해 블루투스 권한이 필요합니다."
- "위치 권한은 Android 11 이하에서 BLE 스캔에 필요합니다."

### 9.2 개인정보처리방침
BLE를 사용하는 앱은 개인정보처리방침이 필요할 수 있습니다. 간단한 웹페이지를 만들거나 GitHub Pages를 사용할 수 있습니다.

### 9.3 앱 아이콘
현재 앱에 아이콘이 없으므로 반드시 추가해야 합니다:
- `app/src/main/res/mipmap-*/ic_launcher.png` 파일 생성
- 또는 Android Studio의 Image Asset Studio 사용

## 10. 빠른 체크리스트

- [ ] Google Play Console 계정 생성 및 개발자 등록 ($25)
- [ ] 키스토어 파일 생성 및 안전하게 보관
- [ ] build.gradle에 서명 설정 추가
- [ ] 릴리즈 AAB 빌드
- [ ] 앱 아이콘 생성 (512x512px)
- [ ] 스크린샷 2개 이상 준비
- [ ] 앱 설명 작성
- [ ] 개인정보처리방침 준비 (필요시)
- [ ] Google Play Console에 앱 정보 입력
- [ ] AAB 파일 업로드
- [ ] 검토 제출

## 11. 유용한 링크

- Google Play Console: https://play.google.com/console
- Android 개발자 가이드: https://developer.android.com/distribute/googleplay/start
- 앱 서명 가이드: https://developer.android.com/studio/publish/app-signing
- Material Design 아이콘: https://fonts.google.com/icons

---

**참고:** 이 가이드는 2024년 기준입니다. Google Play 정책은 변경될 수 있으므로 최신 정보는 Google Play Console의 도움말을 참조하세요.




