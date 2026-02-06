# APK 빌드 스크립트
# 이 스크립트는 디버그 APK를 빌드합니다

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "APK 빌드 시작" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

Write-Host "프로젝트 경로: $projectRoot" -ForegroundColor Yellow
Write-Host ""

# Gradle Wrapper 확인
$gradlew = Join-Path $projectRoot "gradlew.bat"
if (-not (Test-Path $gradlew)) {
    Write-Host "경고: gradlew.bat 파일을 찾을 수 없습니다." -ForegroundColor Yellow
    Write-Host "Android Studio에서 프로젝트를 한 번 열면 자동 생성됩니다." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "대신 Android Studio에서 직접 빌드하세요:" -ForegroundColor Cyan
    Write-Host "  Build > Build Bundle(s) / APK(s) > Build APK(s)" -ForegroundColor White
    Write-Host ""
    Read-Host "아무 키나 눌러 종료하세요"
    exit 1
}

Write-Host "APK 빌드 중..." -ForegroundColor Green
Write-Host ""

# 디버그 APK 빌드
& $gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "APK 빌드 완료!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    
    $apkPath = Join-Path $projectRoot "app\build\outputs\apk\debug\app-debug.apk"
    
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length / 1MB
        Write-Host "APK 파일 위치: $apkPath" -ForegroundColor Yellow
        Write-Host "파일 크기: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "다음 단계:" -ForegroundColor Cyan
        Write-Host "1. 이 APK 파일을 스마트폰으로 전송하세요" -ForegroundColor White
        Write-Host "2. 스마트폰에서 '설정 > 보안 > 알 수 없는 소스' 허용" -ForegroundColor White
        Write-Host "3. APK 파일 실행하여 설치" -ForegroundColor White
        Write-Host ""
        Write-Host "또는 USB로 연결하여 Android Studio에서 직접 설치:" -ForegroundColor Cyan
        Write-Host "  Run 버튼 클릭 (Shift+F10)" -ForegroundColor White
        Write-Host ""
        
        # 파일 탐색기에서 열기 옵션
        $open = Read-Host "파일 탐색기에서 APK 폴더 열기? (Y/N)"
        if ($open -eq "Y" -or $open -eq "y") {
            $apkFolder = Split-Path $apkPath
            Invoke-Item $apkFolder
        }
    } else {
        Write-Host "경고: APK 파일을 찾을 수 없습니다." -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "빌드 실패!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "오류를 확인하고 다시 시도하세요." -ForegroundColor Yellow
    Write-Host "Android Studio에서 빌드하면 더 자세한 오류 정보를 볼 수 있습니다." -ForegroundColor Yellow
}

Write-Host ""
Read-Host "아무 키나 눌러 종료하세요"




