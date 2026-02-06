# 현재 폴더를 Android Studio 프로젝트로 변환하는 스크립트
# 이 스크립트는 현재 폴더에 Android Studio 프로젝트 구조를 생성합니다

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Android Studio 프로젝트 구조 생성" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "프로젝트 루트: $projectRoot" -ForegroundColor Yellow
Write-Host ""

# 기존 파일들 확인
$mainActivity = Join-Path $projectRoot "MainActivity.kt"
$activityMain = Join-Path $projectRoot "activity_main.xml"
$manifest = Join-Path $projectRoot "AndroidManifest.xml"
$buildGradle = Join-Path $projectRoot "build.gradle"

if (-not (Test-Path $mainActivity)) {
    Write-Host "오류: MainActivity.kt 파일을 찾을 수 없습니다!" -ForegroundColor Red
    Read-Host "아무 키나 눌러 종료하세요"
    exit 1
}

Write-Host "프로젝트 구조 생성 중..." -ForegroundColor Green
Write-Host ""

# 1. app 폴더 구조 생성
$appPath = Join-Path $projectRoot "app"
$appSrcPath = Join-Path $appPath "src\main"
$javaPath = Join-Path $appSrcPath "java\com\example\rgbsensorble"
$resPath = Join-Path $appSrcPath "res"
$layoutPath = Join-Path $resPath "layout"

# 폴더 생성
New-Item -ItemType Directory -Path $javaPath -Force | Out-Null
New-Item -ItemType Directory -Path $layoutPath -Force | Out-Null
Write-Host "✓ 폴더 구조 생성 완료" -ForegroundColor Green

# 2. 파일 이동
Write-Host ""
Write-Host "파일 이동 중..." -ForegroundColor Green

# MainActivity.kt 이동
if (Test-Path $mainActivity) {
    $destMainActivity = Join-Path $javaPath "MainActivity.kt"
    Copy-Item -Path $mainActivity -Destination $destMainActivity -Force
    Write-Host "✓ MainActivity.kt 이동 완료" -ForegroundColor Green
}

# activity_main.xml 이동
if (Test-Path $activityMain) {
    $destActivityMain = Join-Path $layoutPath "activity_main.xml"
    Copy-Item -Path $activityMain -Destination $destActivityMain -Force
    Write-Host "✓ activity_main.xml 이동 완료" -ForegroundColor Green
}

# AndroidManifest.xml 이동
if (Test-Path $manifest) {
    $destManifest = Join-Path $appSrcPath "AndroidManifest.xml"
    Copy-Item -Path $manifest -Destination $destManifest -Force
    Write-Host "✓ AndroidManifest.xml 이동 완료" -ForegroundColor Green
}

# build.gradle 이동 (app/build.gradle)
if (Test-Path $buildGradle) {
    $destBuildGradle = Join-Path $appPath "build.gradle"
    Copy-Item -Path $buildGradle -Destination $destBuildGradle -Force
    Write-Host "✓ build.gradle 이동 완료" -ForegroundColor Green
}

# 3. 루트 build.gradle 생성
Write-Host ""
Write-Host "프로젝트 설정 파일 생성 중..." -ForegroundColor Green

$rootBuildGradle = Join-Path $projectRoot "build.gradle"
$rootBuildGradleContent = @"
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    ext.kotlin_version = '1.9.22'
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.0'
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:`$kotlin_version"
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
"@

Set-Content -Path $rootBuildGradle -Value $rootBuildGradleContent -Encoding UTF8
Write-Host "✓ 루트 build.gradle 생성 완료" -ForegroundColor Green

# 4. settings.gradle 생성
$settingsGradle = Join-Path $projectRoot "settings.gradle"
$settingsGradleContent = @"
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RGBSensorBLE"
include ':app'
"@

Set-Content -Path $settingsGradle -Value $settingsGradleContent -Encoding UTF8
Write-Host "✓ settings.gradle 생성 완료" -ForegroundColor Green

# 5. gradle.properties 생성
$gradleProperties = Join-Path $projectRoot "gradle.properties"
$gradlePropertiesContent = @"
# Project-wide Gradle settings.
# IDE (e.g. Android Studio) users:
# Gradle settings configured through the IDE *will override*
# any settings specified in this file.
# For more details on how to configure your build environment visit
# http://www.gradle.org/docs/current/userguide/build_environment.html
# Specifies the JVM arguments used for the daemon process.
# The setting is particularly useful for tweaking memory settings.
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
# When configured, Gradle will run in incubating parallel mode.
# This option should only be used with decoupled projects. More details, visit
# http://www.gradle.org/docs/current/userguide/multi_project_builds.html#sec:decoupled_projects
# org.gradle.parallel=true
# AndroidX package structure to make it clearer which packages are bundled with the
# Android operating system, and which are packaged with your app's APK
# https://developer.android.com/topic/libraries/support-library/androidx-rn
android.useAndroidX=true
# Kotlin code style for this project: "official" or "obsolete":
kotlin.code.style=official
# Enables namespacing of each library's R class so that its R class includes only the
# resources declared in the library itself and none from the library's dependencies,
# thereby reducing the size of the R class for that library
android.nonTransitiveRClass=true
"@

Set-Content -Path $gradleProperties -Value $gradlePropertiesContent -Encoding UTF8
Write-Host "✓ gradle.properties 생성 완료" -ForegroundColor Green

# 6. .gitignore 생성 (선택사항)
$gitignore = Join-Path $projectRoot ".gitignore"
if (-not (Test-Path $gitignore)) {
    $gitignoreContent = @"
*.iml
.gradle
/local.properties
/.idea/caches
/.idea/libraries
/.idea/modules.xml
/.idea/workspace.xml
/.idea/navEditor.xml
/.idea/assetWizardSettings.xml
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
"@
    Set-Content -Path $gitignore -Value $gitignoreContent -Encoding UTF8
    Write-Host "✓ .gitignore 생성 완료" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "프로젝트 구조 생성 완료!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "다음 단계:" -ForegroundColor Yellow
Write-Host "1. Android Studio 실행" -ForegroundColor White
Write-Host "2. File > Open > 이 폴더 선택 ($projectRoot)" -ForegroundColor White
Write-Host "3. Android Studio가 자동으로 프로젝트를 인식합니다" -ForegroundColor White
Write-Host "4. Gradle 동기화 대기 (자동으로 시작됨)" -ForegroundColor White
Write-Host "5. 빌드 및 실행!" -ForegroundColor White
Write-Host ""
Write-Host "⚠️ 참고: 원본 파일들은 그대로 유지됩니다." -ForegroundColor Yellow
Write-Host "   필요하면 나중에 삭제하셔도 됩니다." -ForegroundColor Yellow
Write-Host ""
Read-Host "아무 키나 눌러 종료하세요"




