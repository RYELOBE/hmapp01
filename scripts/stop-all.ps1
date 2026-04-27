$targets = @("vite", "java", "mvn")
Get-Process | Where-Object { $targets -contains $_.ProcessName.ToLower() } | Stop-Process -Force -ErrorAction SilentlyContinue
Write-Host "Stopped vite/java/mvn processes."
