@echo off
chcp 65001 >nul
title EcoFast
cd /d "%~dp0backend"

where java >nul 2>nul
if errorlevel 1 (
  echo.
  echo  [ERRO] Java nao encontrado. Instale o Java 17 ou mais novo:
  echo         https://adoptium.net
  echo.
  pause
  exit /b 1
)

echo.
echo  Iniciando o EcoFast... (a primeira vez demora alguns minutos para baixar as dependencias)
echo  Quando aparecer "Started EcoFastApplication", abra: http://localhost:8080
echo  Para desligar o sistema, feche esta janela.
echo.

start "" cmd /c "timeout /t 25 >nul & start http://localhost:8080"
call mvnw.cmd spring-boot:run
pause
