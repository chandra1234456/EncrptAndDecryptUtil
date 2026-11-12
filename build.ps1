# Builds dist\CryptoTool\CryptoTool.exe  (run:  powershell -ExecutionPolicy Bypass -File build.ps1)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$J    = 'C:\Program Files\Java\jdk-17.0.3.1\bin'
$lib  = 'D:\IntelliJ IDEA Community Edition 2025.2.5\plugins\Kotlin\kotlinc\lib'
$std  = "$env:USERPROFILE\.m2\repository\org\jetbrains\kotlin\kotlin-stdlib\2.2.20\kotlin-stdlib-2.2.20.jar"
$libs = "libs\gson-2.14.0.jar;libs\jackson-databind-3.1.2.jar"

Remove-Item build, dist -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory build\jar, build\input, build\icons | Out-Null

# 1. compile Kotlin
$files = (Get-ChildItem src -Recurse -Filter *.kt).FullName
$cp = (Get-ChildItem $lib -Filter *.jar).FullName -join ';'
& "$J\java.exe" -cp $cp org.jetbrains.kotlin.cli.jvm.K2JVMCompiler @files -no-stdlib -cp "$libs;$std" -d build\jar -jvm-target 17
if ($LASTEXITCODE) { throw 'compile failed' }

# 2. regenerate icon (CryptoTool.ico) from AppIcon
& "$J\java.exe" -cp "build\jar;$std" AppIcon build\icons
python -c "import struct;S=[16,32,48,64,128,256];D=[open(f'build/icons/icon{s}.png','rb').read() for s in S];o=struct.pack('<HHH',0,1,len(S));f=6+16*len(S)
for s,d in zip(S,D):o+=struct.pack('<BBBBHHII',s%256,s%256,0,0,1,32,len(d),f);f+=len(d)
open('CryptoTool.ico','wb').write(o+b''.join(D))"

# 3. fat jar
Push-Location build\jar
foreach ($d in @($std, "..\..\libs\gson-2.14.0.jar", "..\..\libs\jackson-databind-3.1.2.jar")) { & "$J\jar.exe" xf (Resolve-Path $d) }
Remove-Item META-INF\*.SF, META-INF\*.RSA, META-INF\*.DSA -ErrorAction SilentlyContinue
Pop-Location
"Main-Class: SimpleUI`n" | Set-Content build\manifest.txt -Encoding ascii
& "$J\jar.exe" cfm build\input\CryptoTool.jar build\manifest.txt -C build\jar .

# 4. native exe with bundled runtime + icon
& "$J\jpackage.exe" --type app-image --name CryptoTool --input build\input --main-jar CryptoTool.jar `
  --main-class SimpleUI --icon CryptoTool.ico --dest dist --app-version 1.0 `
  --java-options "-Xshare:auto -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xmx256m"
Write-Host "Done: dist\CryptoTool\CryptoTool.exe"
