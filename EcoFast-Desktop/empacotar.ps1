# Gera o aplicativo pronto para usar: dist\EcoFast\EcoFast.exe (com o Java embutido).
# Uso (no PowerShell, dentro desta pasta):  .\empacotar.ps1
# Os programas de linha de comando (mvn, java, jpackage) escrevem avisos no canal de erro; o
# PowerShell 5 trata isso como falha. Por isso conferimos o codigo de saida de cada um.
$ErrorActionPreference = 'Continue'
Set-Location $PSScriptRoot

$mvn = if (Get-Command mvn -ErrorAction SilentlyContinue) { 'mvn' } else { '.\mvnw.cmd' }
if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    throw 'jpackage nao encontrado. Instale o JDK 17 ou mais novo (https://adoptium.net).'
}

Write-Host '1/4  Compilando e juntando tudo em um JAR...'
& $mvn -q -B clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'A compilacao falhou.' }

Write-Host '2/4  Gerando o icone...'
New-Item -ItemType Directory -Force build | Out-Null
java -cp target\classes br.com.ecofast.ui.Icone build\icon.ico
if ($LASTEXITCODE -ne 0) { throw 'Nao consegui gerar o icone.' }

Write-Host '3/4  Montando o aplicativo com o Java embutido...'
Remove-Item build\entrada, dist\EcoFast -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force build\entrada, dist | Out-Null
Copy-Item target\ecofast.jar build\entrada\ecofast.jar

jpackage --type app-image --name EcoFast --app-version 1.0.0 `
    --input build\entrada --main-jar ecofast.jar --main-class br.com.ecofast.Main `
    --icon build\icon.ico --dest dist `
    --vendor 'Gustavo - UniAnchieta' --description 'EcoFast - gestao de ecopontos com EcoCoins' `
    --java-options '-Dfile.encoding=UTF-8' `
    --add-modules 'java.desktop,java.sql,java.naming,java.logging,java.management,java.security.jgss,java.xml,jdk.crypto.ec,jdk.unsupported,jdk.localedata' `
    --jlink-options '--include-locales=en,pt'
if ($LASTEXITCODE -ne 0) { throw 'O jpackage falhou.' }

Write-Host '4/4  Pronto!'
$exe = Resolve-Path dist\EcoFast\EcoFast.exe
Write-Host "Aplicativo: $exe"
