$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw "No se encontro jpackage. Instala un JDK 17 o superior."
}

mvn clean package dependency:copy-dependencies `
    "-DincludeScope=runtime" `
    "-DoutputDirectory=target/jpackage-input/lib"

$mainJar = Get-ChildItem target -Filter "*.jar" -File |
    Where-Object { $_.Name -notlike "*-sources.jar" } |
    Select-Object -First 1

if ($null -eq $mainJar) {
    throw "No se encontro el jar del cliente."
}

Copy-Item $mainJar.FullName "target/jpackage-input/$($mainJar.Name)" -Force
Remove-Item target/jpackage-output -Recurse -Force -ErrorAction SilentlyContinue
New-Item target/jpackage-output -ItemType Directory | Out-Null

jpackage `
    --type exe `
    --name "CRM Asociaciones" `
    --app-version 1.0.0 `
    --input target/jpackage-input `
    --main-jar $mainJar.Name `
    --main-class com.aitsolutions.crmclient.Launcher `
    --dest target/jpackage-output `
    --win-menu `
    --win-shortcut `
    --win-dir-chooser

Write-Host "Instalador generado en target/jpackage-output/"
