# install-wrapper.ps1
# Find the 3 just-downloaded Gradle wrapper files in Downloads (handling
# Chrome's "(1)", "(2)" suffixes if you triggered the download more than once)
# and move them into the right places in the project.
#
# Usage:
#   cd "C:\Users\Thin\Desktop\to_do_list"
#   powershell -ExecutionPolicy Bypass -File install-wrapper.ps1

$proj      = "C:\Users\Thin\Desktop\to_do_list"
$downloads = "$env:USERPROFILE\Downloads"
$wrapDir   = "$proj\gradle\wrapper"

Write-Host ""
Write-Host "Looking in: $downloads"
Write-Host ""

if (-not (Test-Path $wrapDir)) {
    New-Item -ItemType Directory -Force -Path $wrapDir | Out-Null
}

# Patterns to find each file (most-recent match wins)
$patterns = @(
    @{ pattern = "gradlew";              dst = "$proj\gradlew";                     mustEnd = $false },
    @{ pattern = "gradlew.bat*";         dst = "$proj\gradlew.bat";                 mustEnd = $false },
    @{ pattern = "gradle-wrapper*.jar*"; dst = "$wrapDir\gradle-wrapper.jar";       mustEnd = $false }
)

# Special handling for "gradlew" since "gradlew*" would also match "gradlew.bat".
# We match exactly "gradlew" or "gradlew (N)" with no extension.
function Find-GradlewFile {
    Get-ChildItem -Path $downloads -File -ErrorAction SilentlyContinue | Where-Object {
        $_.Name -match '^gradlew(\s\(\d+\))?$'
    } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
}

function Find-GradlewBat {
    Get-ChildItem -Path $downloads -File -ErrorAction SilentlyContinue | Where-Object {
        $_.Name -match '^gradlew\.bat(\s\(\d+\))?$'
    } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
}

function Find-WrapperJar {
    Get-ChildItem -Path $downloads -File -ErrorAction SilentlyContinue | Where-Object {
        $_.Name -match '^gradle-wrapper(\s\(\d+\))?\.jar$'
    } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
}

$tasks = @(
    @{ name = "gradlew";            finder = ${function:Find-GradlewFile}; dst = "$proj\gradlew" },
    @{ name = "gradlew.bat";        finder = ${function:Find-GradlewBat};  dst = "$proj\gradlew.bat" },
    @{ name = "gradle-wrapper.jar"; finder = ${function:Find-WrapperJar};  dst = "$wrapDir\gradle-wrapper.jar" }
)

$missing = 0
foreach ($t in $tasks) {
    $hit = & $t.finder
    if ($null -eq $hit) {
        Write-Host "MISSING: $($t.name)"
        $missing++
        continue
    }
    Move-Item -Path $hit.FullName -Destination $t.dst -Force
    $size = (Get-Item $t.dst).Length
    Write-Host "OK  $($hit.Name)  ->  $($t.dst)  ($size bytes)"
}

Write-Host ""
if ($missing -gt 0) {
    Write-Host "Some files were not found. Listing everything in Downloads matching 'gradle*':"
    Get-ChildItem $downloads -Filter "gradle*" -ErrorAction SilentlyContinue | ForEach-Object {
        Write-Host "  $($_.Name)  ($($_.Length) bytes, $($_.LastWriteTime))"
    }
} else {
    Write-Host "All 4 wrapper files in place:"
    Get-ChildItem $proj -Filter "gradlew*" | ForEach-Object { Write-Host "  $($_.FullName)" }
    Get-ChildItem $wrapDir                 | ForEach-Object { Write-Host "  $($_.FullName)" }
}
