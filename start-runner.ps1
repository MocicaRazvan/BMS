Set-Location C:\WINDOWS\system32\actions-runner

$runnerProcess = Get-Process -Name "Runner.Listener" -ErrorAction SilentlyContinue

if ($runnerProcess)
{
    Write-Output "GitHub Actions Runner is already running."
}
else
{
    Write-Output "Starting GitHub Actions Runner..."
    Start-Process -FilePath ".\run.cmd" -Verb RunAs
}
