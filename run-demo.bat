@echo off
setlocal enabledelayedexpansion

REM Jolt JSON转换演示运行脚本 (Windows版本)
REM 
REM 使用方法:
REM   run-demo.bat [选项]
REM
REM 选项:
REM   compile  - 编译项目
REM   test     - 运行测试
REM   run      - 运行演示
REM   package  - 打包项目
REM   clean    - 清理项目
REM   all      - 执行完整流程（清理、编译、测试、打包、运行）

set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=all"

echo === Jolt JSON转换演示项目 ===
echo.

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java未安装或未在PATH中
    exit /b 1
)

for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr "version"') do (
    set "JAVA_VERSION=%%i"
    set "JAVA_VERSION=!JAVA_VERSION:"=!"
)
echo [INFO] Java版本: !JAVA_VERSION!

REM 检查Maven环境
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven未安装或未在PATH中
    exit /b 1
)

for /f "tokens=3" %%i in ('mvn -version 2^>^&1 ^| findstr "Apache Maven"') do (
    set "MAVEN_VERSION=%%i"
)
echo [INFO] Maven版本: !MAVEN_VERSION!
echo.

REM 根据参数执行相应操作
if "%ACTION%"=="clean" goto :clean
if "%ACTION%"=="compile" goto :compile
if "%ACTION%"=="test" goto :test
if "%ACTION%"=="package" goto :package
if "%ACTION%"=="run" goto :run
if "%ACTION%"=="all" goto :all
if "%ACTION%"=="help" goto :help
if "%ACTION%"=="-h" goto :help
if "%ACTION%"=="--help" goto :help

echo [ERROR] 未知选项: %ACTION%
goto :help

:clean
echo [INFO] 清理项目...
call mvn clean
if errorlevel 1 (
    echo [ERROR] 项目清理失败
    exit /b 1
)
echo [SUCCESS] 项目清理完成
goto :end

:compile
echo [INFO] 编译项目...
call mvn compile
if errorlevel 1 (
    echo [ERROR] 项目编译失败
    exit /b 1
)
echo [SUCCESS] 项目编译完成
goto :end

:test
echo [INFO] 运行测试...
call mvn test
if errorlevel 1 (
    echo [ERROR] 测试运行失败
    exit /b 1
)
echo [SUCCESS] 测试运行完成

REM 显示测试结果摘要
if exist "target\surefire-reports\TEST-*.xml" (
    echo [INFO] 测试结果摘要:
    REM 这里可以添加更详细的测试结果解析
    echo   检查 target\surefire-reports\ 目录查看详细测试报告
)
goto :end

:package
echo [INFO] 打包项目...
call mvn package -DskipTests
if errorlevel 1 (
    echo [ERROR] 项目打包失败
    exit /b 1
)
echo [SUCCESS] 项目打包完成

if exist "target\jolt-demo-1.0.0.jar" (
    for %%i in ("target\jolt-demo-1.0.0.jar") do (
        set "JAR_SIZE=%%~zi"
        set /a "JAR_SIZE_KB=!JAR_SIZE!/1024"
        echo [INFO] 生成的JAR文件大小: !JAR_SIZE_KB! KB
    )
)
goto :end

:run
echo [INFO] 运行Jolt转换演示...

if exist "target\jolt-demo-1.0.0.jar" (
    java -jar target\jolt-demo-1.0.0.jar
) else (
    call mvn exec:java -Dexec.mainClass="com.example.jolt.demo.JoltTransformationDemo"
)

if errorlevel 1 (
    echo [ERROR] 演示运行失败
    exit /b 1
)
echo [SUCCESS] 演示运行完成
goto :end

:all
call :clean
if errorlevel 1 exit /b 1

call :compile
if errorlevel 1 exit /b 1

call :test
if errorlevel 1 exit /b 1

call :package
if errorlevel 1 exit /b 1

call :run
if errorlevel 1 exit /b 1

goto :end

:help
echo Jolt JSON转换演示运行脚本
echo.
echo 使用方法:
echo   %~nx0 [选项]
echo.
echo 选项:
echo   compile  - 编译项目
echo   test     - 运行测试
echo   run      - 运行演示
echo   package  - 打包项目
echo   clean    - 清理项目
echo   all      - 执行完整流程（清理、编译、测试、打包、运行）
echo   help     - 显示此帮助信息
echo.
echo 示例:
echo   %~nx0 all       # 执行完整流程
echo   %~nx0 run       # 仅运行演示
echo   %~nx0 test      # 仅运行测试
goto :end

:end
echo.
echo [SUCCESS] 操作完成!
pause
