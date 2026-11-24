#requires -Version 5.1
<#
.SYNOPSIS
    Prepara un database PostgreSQL locale per il gestionale di calibrazione.

.DESCRIPTION
    Crea (se mancano) il ruolo e il database applicativi, applica
    src/main/resources/db/schema.sql e verifica che le tabelle esistano.
    Lo schema e' idempotente: puo' essere rieseguito senza perdere dati.

    Non serve Docker: usa il PostgreSQL gia' installato sulla macchina.

.PARAMETER PgBin
    Cartella dei binari PostgreSQL, ad esempio "C:\Program Files\PostgreSQL\19\bin".
    Se omesso, lo script cerca psql.exe nel PATH e poi in C:\Program Files\PostgreSQL.

.PARAMETER SuperUser
    Utente amministratore del cluster (default: postgres).

.PARAMETER SuperPassword
    Password dell'utente amministratore. Se omessa viene chiesta in modo sicuro.
    In alternativa si puo' impostare la variabile d'ambiente PGPASSWORD.

.PARAMETER DbName
    Nome del database applicativo (default: calibrazione).

.PARAMETER DbUser
    Ruolo applicativo (default: calibrazione).

.PARAMETER DbPassword
    Password del ruolo applicativo (default: calibrazione).

.EXAMPLE
    .\scripts\setup-postgres.ps1

.EXAMPLE
    .\scripts\setup-postgres.ps1 -PgBin "C:\Program Files\PostgreSQL\19\bin" -DbPassword "unaPasswordSicura"
#>
[CmdletBinding()]
param(
    [string]$PgBin,
    [string]$DbHost = "localhost",
    [int]$DbPort = 5432,
    [string]$SuperUser = "postgres",
    [string]$SuperPassword,
    [string]$DbName = "calibrazione",
    [string]$DbUser = "calibrazione",
    [string]$DbPassword = "calibrazione"
)

$ErrorActionPreference = "Stop"

function Resolve-PsqlPath {
    param([string]$Directory)

    if ($Directory) {
        $exe = Join-Path $Directory "psql.exe"
        if (Test-Path $exe) {
            return $exe
        }
        throw "psql.exe non trovato in '$Directory'."
    }

    $comando = Get-Command psql.exe -ErrorAction SilentlyContinue
    if ($comando) {
        return $comando.Source
    }

    $base = "C:\Program Files\PostgreSQL"
    if (Test-Path $base) {
        $cartelle = Get-ChildItem -Path $base -Directory |
            Where-Object { $_.Name -match '^\d+$' -and (Test-Path (Join-Path $_.FullName "bin\psql.exe")) } |
            Sort-Object { [int]$_.Name } -Descending
        if ($cartelle.Count -gt 0) {
            return (Join-Path $cartelle[0].FullName "bin\psql.exe")
        }
    }

    throw "psql.exe non trovato. Installare PostgreSQL oppure passare -PgBin 'C:\Program Files\PostgreSQL\19\bin'."
}

function SqlLiteral {
    param([string]$Valore)
    return "'" + $Valore.Replace("'", "''") + "'"
}

function SqlIdentifier {
    param([string]$Valore)
    return '"' + $Valore.Replace('"', '""') + '"'
}

function Invoke-Psql {
    param(
        [string]$Utente,
        [string]$Database,
        [string]$Sql
    )

    $output = & $script:Psql -h $DbHost -p $DbPort -U $Utente -d $Database -tAqc $Sql
    if ($LASTEXITCODE -ne 0) {
        throw "Comando psql fallito (utente=$Utente, database=$Database)."
    }
    return ($output | Out-String).Trim()
}

$script:Psql = Resolve-PsqlPath -Directory $PgBin
$schemaPath = Join-Path (Split-Path $PSScriptRoot -Parent) "src\main\resources\db\schema.sql"

if (-not (Test-Path $schemaPath)) {
    throw "Schema non trovato: $schemaPath"
}

if (-not $SuperPassword) {
    if ($env:PGPASSWORD) {
        $SuperPassword = $env:PGPASSWORD
    } else {
        $sicura = Read-Host "Password dell'utente '$SuperUser'" -AsSecureString
        $SuperPassword = [System.Net.NetworkCredential]::new("", $sicura).Password
    }
}

Write-Host ""
Write-Host "PostgreSQL : $script:Psql"
Write-Host "Server     : ${DbHost}:${DbPort}"
Write-Host "Database   : $DbName (owner $DbUser)"
Write-Host ""

$passwordPrecedente = $env:PGPASSWORD

try {
    $env:PGPASSWORD = $SuperPassword
    $versione = Invoke-Psql -Utente $SuperUser -Database "postgres" -Sql "SHOW server_version"
    Write-Host "Connessione riuscita. Versione server: $versione"

    $ruoloEsiste = Invoke-Psql -Utente $SuperUser -Database "postgres" `
        -Sql "SELECT 1 FROM pg_roles WHERE rolname = $(SqlLiteral $DbUser)"
    if ($ruoloEsiste -eq "1") {
        Write-Host "Ruolo '$DbUser' gia' presente."
    } else {
        Invoke-Psql -Utente $SuperUser -Database "postgres" `
            -Sql "CREATE ROLE $(SqlIdentifier $DbUser) LOGIN PASSWORD $(SqlLiteral $DbPassword)" | Out-Null
        Write-Host "Ruolo '$DbUser' creato."
    }

    $databaseEsiste = Invoke-Psql -Utente $SuperUser -Database "postgres" `
        -Sql "SELECT 1 FROM pg_database WHERE datname = $(SqlLiteral $DbName)"
    if ($databaseEsiste -eq "1") {
        Write-Host "Database '$DbName' gia' presente."
    } else {
        Invoke-Psql -Utente $SuperUser -Database "postgres" `
            -Sql "CREATE DATABASE $(SqlIdentifier $DbName) OWNER $(SqlIdentifier $DbUser)" | Out-Null
        Write-Host "Database '$DbName' creato."
    }

    # Applica lo schema come utente applicativo, cosi' le tabelle sono sue.
    $env:PGPASSWORD = $DbPassword

    & $script:Psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -tAqc "SELECT 1" | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Impossibile connettersi come '$DbUser'. Se il ruolo esiste gia' con un'altra password, aggiornala con: ALTER ROLE $(SqlIdentifier $DbUser) PASSWORD '...';"
    }

    Write-Host "Applico lo schema..."
    & $script:Psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -v ON_ERROR_STOP=1 -f $schemaPath | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Applicazione dello schema fallita."
    }

    $tabelle = Invoke-Psql -Utente $DbUser -Database $DbName -Sql @"
SELECT count(*)
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('utenti', 'televisori', 'calibrazioni',
                     'calibrazione_valori', 'calibrazione_parametri', 'calibrazione_colori')
"@

    if ($tabelle -ne "6") {
        throw "Schema incompleto: trovate $tabelle tabelle su 6."
    }

    Write-Host ""
    Write-Host "Setup completato: 6 tabelle presenti." -ForegroundColor Green
    Write-Host ""
    Write-Host "Prossimo passo:"
    Write-Host "  cd '$(Split-Path $PSScriptRoot -Parent)'"
    Write-Host "  .\scripts\run-postgres.ps1"
    Write-Host ""
    Write-Host "Al primo avvio viene creato l'amministratore:"
    Write-Host "  email    : admin@media-saturn.com"
    Write-Host "  password : Admin123! (da cambiare)"
    Write-Host ""
} finally {
    $env:PGPASSWORD = $passwordPrecedente
    if ($null -eq $passwordPrecedente) {
        Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
    }
}
