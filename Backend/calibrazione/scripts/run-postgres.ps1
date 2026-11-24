#requires -Version 5.1
<#
.SYNOPSIS
    Avvia il backend con il profilo PostgreSQL.

.DESCRIPTION
    Imposta le variabili d'ambiente del profilo "postgres" e lancia
    .\gradlew.bat bootRun. Il database deve essere gia' stato preparato con
    .\scripts\setup-postgres.ps1.

.PARAMETER Demo
    Carica i dati dimostrativi (5 utenti e 20 TV) se le tabelle sono vuote.

.PARAMETER DbUrl
    JDBC URL (default: jdbc:postgresql://localhost:5432/calibrazione).

.PARAMETER DbUser
    Utente database (default: calibrazione).

.PARAMETER DbPassword
    Password database (default: calibrazione).

.PARAMETER JwtSecret
    Chiave JWT. Se omessa resta quella di sviluppo in application.properties.

.PARAMETER Port
    Porta HTTP del backend (default: 8080).

.EXAMPLE
    .\scripts\run-postgres.ps1

.EXAMPLE
    .\scripts\run-postgres.ps1 -Demo
#>
[CmdletBinding()]
param(
    [switch]$Demo,
    [string]$DbUrl = "jdbc:postgresql://localhost:5432/calibrazione",
    [string]$DbUser = "calibrazione",
    [string]$DbPassword = "calibrazione",
    [string]$JwtSecret,
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$radice = Split-Path $PSScriptRoot -Parent
$gradle = Join-Path $radice "gradlew.bat"

if (-not (Test-Path $gradle)) {
    throw "gradlew.bat non trovato in '$radice'."
}

$env:SPRING_PROFILES_ACTIVE = "postgres"
$env:DB_URL = $DbUrl
$env:DB_USER = $DbUser
$env:DB_PASSWORD = $DbPassword

if ($JwtSecret) {
    $env:JWT_SECRET = $JwtSecret
}

if ($Demo) {
    $env:APP_SEED_DEMO = "true"
}

Write-Host ""
Write-Host "Profilo   : postgres"
Write-Host "Database  : $DbUrl"
Write-Host "Utente DB : $DbUser"
Write-Host "Dati demo : $(if ($Demo) { 'si' } else { 'no' })"
Write-Host "Backend   : http://localhost:$Port/api"
Write-Host ""

Push-Location $radice
try {
    & $gradle bootRun --console=plain "--args=--server.port=$Port"
} finally {
    Pop-Location
}
