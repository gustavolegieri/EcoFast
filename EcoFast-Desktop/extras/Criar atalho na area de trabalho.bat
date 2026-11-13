@echo off
powershell -NoProfile -ExecutionPolicy Bypass -Command "$d=[Environment]::GetFolderPath('Desktop'); $s=(New-Object -ComObject WScript.Shell).CreateShortcut($d+'\EcoFast.lnk'); $s.TargetPath='%~dp0EcoFast\EcoFast.exe'; $s.WorkingDirectory='%~dp0EcoFast'; $s.IconLocation='%~dp0EcoFast\EcoFast.exe'; $s.Description='EcoFast'; $s.Save()"
echo.
echo Atalho "EcoFast" criado na area de trabalho.
pause
