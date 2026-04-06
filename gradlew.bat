@echo off
rem Gradle Wrapper launcher script for Windows — AetherConnect

set GRADLE_WRAPPER_JAR=gradle\wrapper\gradle-wrapper.jar

if not exist "%GRADLE_WRAPPER_JAR%" (
    echo Please open this project in Android Studio.
    echo It will automatically download and configure Gradle.
    exit /b 1
)

java -jar "%GRADLE_WRAPPER_JAR%" %*
