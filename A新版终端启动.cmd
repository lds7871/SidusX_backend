@echo off
start "" "C:\Users\Administrator\Desktop\ServerSync\terminal-1.24.3504.0\WindowsTerminal.exe" -d "%CD%" --title "GH" cmd.exe /k "mvn spring-boot:run -Dspring-boot.run.jvmArguments=\"-Dfile.encoding=GBK\""
