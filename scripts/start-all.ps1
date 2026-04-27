$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot

Write-Host "Starting frontend micro-apps..."
Start-Process powershell -WindowStyle Hidden -ArgumentList "-NoExit", "-Command", "cd '$root'; pnpm dev:front"

Write-Host "Starting Java backend..."
Start-Process powershell -WindowStyle Hidden -ArgumentList "-NoExit", "-Command", "cd '$root\\backend'; mvn spring-boot:run"

Write-Host "All services started in background. shell: http://localhost:7100"
