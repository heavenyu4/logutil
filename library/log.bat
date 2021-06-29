@echo off

setlocal enabledelayedexpansion
set "year=%date:~0,4%"
set "month=%date:~5,2%"
set "day=%date:~8,2%"
rem echo %time%
set "hour_ten=%time:~0,1%"
rem echo %hour_ten%
set "hour_one=%time:~1,1%"
set "minute=%time:~3,2%"
set "second=%time:~6,2%"
if "%hour_ten%" == " " (
    set DateTime=%year%%month%%day%_0%hour_one%%minute%%second%
) else (
    set DateTime=%year%%month%%day%_%hour_ten%%hour_one%%minute%%second%
)
rem echo %DateTime%

@echo on
echo %cd%  
adb.exe logcat -v threadtime >%cd%/log/%DateTime%.log
pause