# Android Studio 프로젝트에 파일 자동 복사 스크립트
# 사용법: PowerShell에서 이 스크립트를 실행하고 새로 만든 프로젝트 경로를 입력하세요

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android Studio 프로젝트 파일 자동 복사" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 현재 스크립트가 있는 폴더 (소스 파일 위치)
$sourceFolder = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "소스 파일 위치: $sourceFolder" -ForegroundColor Yellow
Write-Host ""

# 새로 만든 Android Studio 프로젝트 경로 입력
$projectPath = Read-Host "새로 만든 Android Studio 프로젝트 경로를 입력하세요 (예: C:\Users\BSA\AndroidStudioProjects\RGBSensorBLE)"

if (-not (Test-Path $projectPath)) {
    Write-Host "오류: 프로젝트 경로를 찾을 수 없습니다: $projectPath" -ForegroundColor Red
    Read-Host "아무 키나 눌러 종료하세요"
    exit 1
}

# 대상 경로 설정
$appPath = Join-Path $projectPath "app"
$srcMainPath = Join-Path $appPath "src\main"
$javaPath = Join-Path $srcMainPath "java\com\example\rgbsensorble"
$resPath = Join-Path $srcMainPath "res"
$layoutPath = Join-Path $resPath "layout"

Write-Host ""
Write-Host "파일 복사 시작..." -ForegroundColor Green
Write-Host ""

# 1. layout 폴더 생성 (없는 경우)
if (-not (Test-Path $layoutPath)) {
    Write-Host "layout 폴더 생성 중..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $layoutPath -Force | Out-Null
    Write-Host "✓ layout 폴더 생성 완료" -ForegroundColor Green
}

# 2. MainActivity.kt 복사
$mainActivitySource = Join-Path $sourceFolder "MainActivity.kt"
$mainActivityDest = Join-Path $javaPath "MainActivity.kt"
if (Test-Path $mainActivitySource) {
    if (-not (Test-Path $javaPath)) {
        New-Item -ItemType Directory -Path $javaPath -Force | Out-Null
    }
    Copy-Item -Path $mainActivitySource -Destination $mainActivityDest -Force
    Write-Host "✓ MainActivity.kt 복사 완료" -ForegroundColor Green
} else {
    Write-Host "⚠ MainActivity.kt 파일을 찾을 수 없습니다" -ForegroundColor Yellow
}

# 3. activity_main.xml 복사
$activityMainSource = Join-Path $sourceFolder "activity_main.xml"
$activityMainDest = Join-Path $layoutPath "activity_main.xml"
if (Test-Path $activityMainSource) {
    Copy-Item -Path $activityMainSource -Destination $activityMainDest -Force
    Write-Host "✓ activity_main.xml 복사 완료" -ForegroundColor Green
} else {
    Write-Host "⚠ activity_main.xml 파일을 찾을 수 없습니다" -ForegroundColor Yellow
}

# 4. AndroidManifest.xml 복사
$manifestSource = Join-Path $sourceFolder "AndroidManifest.xml"
$manifestDest = Join-Path $srcMainPath "AndroidManifest.xml"
if (Test-Path $manifestSource) {
    Copy-Item -Path $manifestSource -Destination $manifestDest -Force
    Write-Host "✓ AndroidManifest.xml 복사 완료" -ForegroundColor Green
} else {
    Write-Host "⚠ AndroidManifest.xml 파일을 찾을 수 없습니다" -ForegroundColor Yellow
}

# 5. build.gradle dependencies 확인 및 안내
$buildGradleSource = Join-Path $sourceFolder "build.gradle"
$buildGradleDest = Join-Path $appPath "build.gradle"
if (Test-Path $buildGradleSource) {
    Write-Host ""
    Write-Host "⚠ build.gradle 파일은 수동으로 확인이 필요합니다" -ForegroundColor Yellow
    Write-Host "  소스 파일: $buildGradleSource" -ForegroundColor Gray
    Write-Host "  대상 파일: $buildGradleDest" -ForegroundColor Gray
    Write-Host "  dependencies 부분을 비교하여 필요한 의존성을 추가하세요" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "파일 복사 완료!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "다음 단계:" -ForegroundColor Yellow
Write-Host "1. Android Studio에서 프로젝트 열기" -ForegroundColor White
Write-Host "2. File > Sync Project with Gradle Files 실행" -ForegroundColor White
Write-Host "3. build.gradle의 dependencies 확인" -ForegroundColor White
Write-Host ""
Read-Host "아무 키나 눌러 종료하세요"




