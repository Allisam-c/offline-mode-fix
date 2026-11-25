$ErrorActionPreference = 'Stop'

$root      = 'F:\AiHome\Minecraft\offline-mode-fix-neoforge'
$libDir    = 'F:\Minecraft\26.3\客户端\星系\.minecraft\libraries'
$classes   = Join-Path $root 'build\classes'
$libs      = Join-Path $root 'build\libs'
$jarOut    = Join-Path $libs 'offlinemodefix-1.0.0+26.3.jar'

if (Test-Path $classes) { Remove-Item -Recurse -Force $classes }
New-Item -ItemType Directory -Force -Path $classes | Out-Null
New-Item -ItemType Directory -Force -Path $libs | Out-Null

# --- 组装 classpath（与 anvillogin 相同策略）---
$cp = New-Object System.Collections.Generic.List[string]
$cp.Add((Join-Path $libDir 'net\neoforged\minecraft-client-patched\26.3.0.115\minecraft-client-patched-26.3.0.115.jar'))
$cp.Add((Join-Path $libDir 'net\neoforged\neoforge\26.3.0.115\neoforge-26.3.0.115-universal.jar'))
$cp.Add((Join-Path $libDir 'net\neoforged\fancymodloader\loader\12.0.16\loader-12.0.16.jar'))
$cp.Add((Join-Path $libDir 'net\neoforged\bus\8.0.5\bus-8.0.5.jar'))
$cp.Add((Join-Path $libDir 'net\neoforged\mergetool\2.0.7\mergetool-2.0.7-api.jar'))

Get-ChildItem -LiteralPath $libDir -Recurse -Filter *.jar | ForEach-Object {
    $f = $_.FullName
    if ($f -match '26\.3\.0\.110' -or $f -match 'loader-12\.0\.12' -or $f -match 'earlydisplay-12\.0\.12') { return }
    $cp.Add($f)
}
$cpString = ($cp -join ';')

# --- 编译 ---
$sources = Get-ChildItem -LiteralPath (Join-Path $root 'src\main\java') -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$srcFile = Join-Path $root 'build\sources.txt'
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($srcFile, ($sources -join "`n"), $utf8NoBom)

$ErrorActionPreference = 'Continue'
& javac --release 21 -encoding UTF-8 -nowarn -classpath $cpString -d $classes "@$srcFile" 2>&1 | ForEach-Object { $_.ToString() } | Out-File -FilePath (Join-Path $root 'build\javac.log') -Encoding utf8
$javacExit = $LASTEXITCODE
$ErrorActionPreference = 'Stop'
$out = Get-Content -LiteralPath (Join-Path $root 'build\javac.log') -Encoding UTF8
if ($out) { $out | Select-Object -First 40 | ForEach-Object { Write-Output $_ } }
if ($javacExit -ne 0) { throw "javac failed with exit code $javacExit" }
Write-Output "COMPILED -> $classes"

# --- 复制资源 ---
Copy-Item -Recurse -Force (Join-Path $root 'src\main\resources\*') $classes

# --- 打包 ---
if (Test-Path $jarOut) { Remove-Item -Force $jarOut }
& jar --create --file $jarOut -C $classes .
if ($LASTEXITCODE -ne 0) { throw "jar failed with exit code $LASTEXITCODE" }
Write-Output "JAR -> $jarOut"

Add-Type -AssemblyName System.IO.Compression.FileSystem
$z = [System.IO.Compression.ZipFile]::OpenRead($jarOut)
$z.Entries | ForEach-Object { "  $($_.FullName)  $($_.Length)" }
$z.Dispose()
