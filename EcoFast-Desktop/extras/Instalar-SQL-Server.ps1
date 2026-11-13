# Instala o SQL Server 2022 Express (gratuito) já pronto para o EcoFast:
# login por usuario e senha, TCP/IP ligado e porta fixa 1433.
# Precisa de internet, de uns 4 GB livres no disco e de permissao de administrador.
$ErrorActionPreference = 'Stop'

$souAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole(
    [Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $souAdmin) {
    Write-Host 'O Windows vai pedir autorizacao de administrador. Clique em Sim.'
    Start-Process powershell -Verb RunAs -ArgumentList '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', "`"$PSCommandPath`""
    exit
}

try {
    if (Get-Service -Name 'MSSQL$SQLEXPRESS' -ErrorAction SilentlyContinue) {
        Write-Host 'O SQL Server Express (instancia SQLEXPRESS) ja esta instalado neste computador.'
        Write-Host 'Abra o EcoFast e informe: servidor localhost, porta 1433, usuario sa e a senha que voce definiu.'
        return
    }

    Write-Host ''
    Write-Host 'Escolha a senha do usuario "sa" (o administrador do SQL Server).'
    Write-Host 'Use so letras e numeros, com no minimo 8 caracteres, incluindo maiuscula, minuscula e numero.'
    $segura = Read-Host 'Senha' -AsSecureString
    $senha = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura))
    if ($senha -notmatch '^[A-Za-z0-9]{8,}$' -or $senha -cnotmatch '[A-Z]' -or $senha -cnotmatch '[a-z]' -or $senha -notmatch '[0-9]') {
        throw 'Senha fora do padrao pedido (so letras e numeros, 8 ou mais, com maiuscula, minuscula e numero).'
    }

    $url = 'https://download.microsoft.com/download/3/8/d/38de7036-2433-4207-8eae-06e247e17b25/SQLEXPR_x64_ENU.exe'
    $arq = Join-Path $env:TEMP 'SQLEXPR_x64_ENU.exe'
    Write-Host 'Baixando o instalador da Microsoft (cerca de 280 MB)...'
    Invoke-WebRequest -Uri $url -OutFile $arq -UseBasicParsing

    $assinatura = Get-AuthenticodeSignature $arq
    if ($assinatura.Status -ne 'Valid' -or $assinatura.SignerCertificate.Subject -notmatch 'Microsoft Corporation') {
        throw 'O arquivo baixado nao tem assinatura valida da Microsoft. Instalacao cancelada.'
    }

    Write-Host 'Instalando (leva alguns minutos, nao feche esta janela)...'
    $conta = [Security.Principal.WindowsIdentity]::GetCurrent().Name
    $args1 = @('/Q', '/IACCEPTSQLSERVERLICENSETERMS', '/ACTION=Install', '/FEATURES=SQLENGINE',
        '/INSTANCENAME=SQLEXPRESS', '/SECURITYMODE=SQL', "/SAPWD=$senha", "/SQLSYSADMINACCOUNTS=`"$conta`"",
        '/TCPENABLED=1', '/NPENABLED=0', '/UPDATEENABLED=0', '/SQLSVCSTARTUPTYPE=Automatic')
    $p = Start-Process -FilePath $arq -ArgumentList $args1 -Wait -PassThru
    if ($p.ExitCode -ne 0 -and $p.ExitCode -ne 3010) { throw "A instalacao falhou (codigo $($p.ExitCode))." }

    $tcp = 'HKLM:\SOFTWARE\Microsoft\Microsoft SQL Server\MSSQL16.SQLEXPRESS\MSSQLServer\SuperSocketNetLib\Tcp'
    Set-ItemProperty -Path $tcp -Name Enabled -Value 1
    Set-ItemProperty -Path "$tcp\IPAll" -Name TcpPort -Value '1433'
    Set-ItemProperty -Path "$tcp\IPAll" -Name TcpDynamicPorts -Value ''
    Restart-Service -Name 'MSSQL$SQLEXPRESS' -Force

    Remove-Item $arq -Force -ErrorAction SilentlyContinue
    Write-Host ''
    Write-Host 'Pronto! Agora abra o EcoFast e, na tela de conexao, informe:'
    Write-Host '   Servidor: localhost     Porta: 1433     Usuario: sa     Senha: a que voce acabou de escolher'
} catch {
    Write-Host ''
    Write-Host "ERRO: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ''
Read-Host 'Pressione Enter para fechar'
