@echo off

set "HAS_ERROR=0"

set "VS_CODE_PATH=%LOCALAPPDATA%\Programs\Microsoft VS Code\bin\code.cmd"
if not exist "%VS_CODE_PATH%" (
	echo ERROR: VS Code was not found at: "%VS_CODE_PATH%"
	set "HAS_ERROR=1"
)

set "GIT_HUB_PATH=%LOCALAPPDATA%\GitHubDesktop\GitHubDesktop.exe"
if not exist "%GIT_HUB_PATH%" (
	echo ERROR: GitHub Desktop was not found at: "%GIT_HUB_PATH%"
	set "HAS_ERROR=1"
)

if "%HAS_ERROR%"=="1" (
    pause
    exit /b
)

echo Opening Project in VS Code...
call "%VS_CODE_PATH%" "%~dp0" 

echo Opening Github Desktop...
start "" "%GIT_HUB_PATH%" "%~dp0"

echo Opening Trello...
start "" "https://trello.com/b/Sj4RJopE/automata"

exit /b