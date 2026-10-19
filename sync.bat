@echo off
chcp 65001 >nul
title 校园掏 - 数据库结构同步

echo ============================================================
echo   校园掏 - 数据库结构同步工具
echo ============================================================
echo.
echo   用法: sync.bat [MySQL密码]
echo   例如: sync.bat 123456      ^(不填则默认 123456^)
echo.

REM ===================== 1. 定位 mysql.exe =====================
set "MYSQL="
for %%p in (
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe"
    "D:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    "D:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe"
) do (
    if not defined MYSQL (
        if exist %%p set "MYSQL=%%~p"
    )
)

if not defined MYSQL (
    echo 未能自动找到 mysql.exe，常见路径都没有安装记录。
    echo.
    set /p MYSQL=请把 mysql.exe 拖进来，或输入完整路径: 
)

set "MYSQL=%MYSQL:"=%"
echo 使用的 MySQL: %MYSQL%
echo.

REM ===================== 2. 读取密码 =====================
set "PWD=%~1"
if not defined PWD set "PWD=123456"

REM ===================== 3. 基准脚本 =====================
set "ROOT=%~dp0"
echo [1/2] 执行基准脚本 init.sql ...
"%MYSQL%" -uroot -p%PWD% --default-character-set=utf8mb4 < "%ROOT%sql\init.sql" 2>nul
if errorlevel 1 (
    echo       执行异常，请确认密码是否为 %PWD%
    echo       正确用法: sync.bat 你的密码
    goto :fail
)
echo       完成

REM ===================== 4. 增量迁移 =====================
echo [2/2] 执行增量迁移脚本 ...
if not exist "%ROOT%sql\migration\*.sql" (
    echo       没有需要执行的迁移脚本
    goto :ok
)

for %%f in ("%ROOT%sql\migration\*.sql") do (
    echo       - %%~nxf
    "%MYSQL%" -uroot -p%PWD% --default-character-set=utf8mb4 < "%%f" 2>nul
    if errorlevel 1 (
        echo          ^[失败^] %%~nxf
        goto :fail
    )
)

:ok
echo.
echo ============================================================
echo   同步完成！数据库结构已与最新版本一致。
echo.
echo   提示: 本工具只同步【表结构】，不同步账号和商品数据，
echo         各自用自己的账号登录即可。
echo ============================================================
echo.
echo (10 秒后自动关闭窗口)
timeout /t 10 >nul 2>nul
exit /b 0

:fail
echo.
echo ============================================================
echo   同步未完成，请检查上面的错误信息。
echo   常见问题:
echo     1. MySQL 服务没启动
echo     2. 密码不对 (用法: sync.bat 你的密码)
echo     3. MySQL 没安装在默认路径
echo ============================================================
echo.
echo (15 秒后自动关闭窗口)
timeout /t 15 >nul 2>nul
exit /b 1
