$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot
if (-not (Test-Path -LiteralPath 'frontend/node_modules')) { npm --prefix frontend install }
Start-Process -FilePath 'mvn.cmd' -ArgumentList '-f backend/pom.xml spring-boot:run' -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -RedirectStandardOutput "$PSScriptRoot/backend-server.log" -RedirectStandardError "$PSScriptRoot/backend-error.log"
npm --prefix frontend run dev
