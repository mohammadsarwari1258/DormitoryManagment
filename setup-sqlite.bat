@echo off
setlocal
cd /d "%~dp0"
if not exist "lib" mkdir "lib"
echo Downloading SQLite JDBC driver...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; $u='https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.50.3.0/sqlite-jdbc-3.50.3.0.jar';" ^
  "Invoke-WebRequest -Uri $u -OutFile 'lib\sqlite-jdbc-3.50.3.0.jar'"
if exist "lib\sqlite-jdbc-3.50.3.0.jar" (
  echo.
  echo SQLite JDBC driver installed successfully.
  echo You can now open the project in NetBeans and Clean and Build.
) else (
  echo.
  echo Download failed. Check your internet connection and try again.
)
pause
