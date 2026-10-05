Write-Host "Building all microservices..." -ForegroundColor Cyan

$services = @(
    "discover-service\discover-service",
    "user-service",
    "product-service",
    "order-service",
    "notification-service",
    "api-gateway\api-gateway"
)

foreach ($svc in $services) {
    Write-Host "
>>> Building $svc..." -ForegroundColor Yellow
    Push-Location $svc
    .\mvnw.cmd clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build $svc"
        Pop-Location
        exit 1
    }
    Pop-Location
}

Write-Host "
[SUCCESS] All services packaged successfully! Run: docker compose up --build" -ForegroundColor Green
