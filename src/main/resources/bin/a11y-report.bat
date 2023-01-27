@echo off

rem Check if Java is installed
if not defined JAVA_HOME (
    echo Java is not installed. Exiting the script.
    exit /b 1
)

rem Check if java version is greater than or equal to 1.8
for /f "tokens=3 delims=." %%i in ('java -version 2^>^&1 ^| findstr " version"') do (
    if %%i leq 8 (
        echo Java version is greater than or equal to 1.8
    ) else (
        echo Java version is less than 1.8
        echo Java version should be greater or equal to 1.8
        exit /b 1
    )
)

set JAR_PATH=%~dp0
set CLASSPATH=%JAR_PATH%\*

java -classpath "%CLASSPATH%" io.github.sridharbandi.HtmlReporter %*
