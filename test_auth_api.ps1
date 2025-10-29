# QuickPlan 认证系统测试脚本
# 使用PowerShell运行

$baseUrl = "http://localhost:8080"

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "QuickPlan 认证系统测试" -ForegroundColor Cyan
Write-Host "======================================`n" -ForegroundColor Cyan

# 测试1: 健康检查
Write-Host "1. 测试健康检查..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get
    Write-Host "✅ 健康检查成功: $($response.status)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ 健康检查失败: $_" -ForegroundColor Red
    Write-Host ""
}

# 测试2: 发送验证码
Write-Host "2. 测试发送验证码..." -ForegroundColor Yellow
$sendCodeBody = @{
    phone = "13800138000"
    type = "login"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/phone/send-code" -Method Post -Body $sendCodeBody -ContentType "application/json"
    Write-Host "✅ 验证码发送成功: $($response.message)" -ForegroundColor Green
    Write-Host "💡 请在后端日志中查看验证码" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "❌ 发送验证码失败: $_" -ForegroundColor Red
    Write-Host ""
}

# 测试3: 手机号登录(需要先在日志中获取验证码)
Write-Host "3. 测试手机号登录..." -ForegroundColor Yellow
Write-Host "请输入刚才收到的验证码: " -NoNewline
$code = Read-Host

$loginBody = @{
    phone = "13800138000"
    code = $code
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/phone/login" -Method Post -Body $loginBody -ContentType "application/json"
    Write-Host "✅ 登录成功!" -ForegroundColor Green
    Write-Host "用户ID: $($response.data.userId)" -ForegroundColor Cyan
    Write-Host "昵称: $($response.data.userInfo.nickname)" -ForegroundColor Cyan
    Write-Host "Token: $($response.data.token.Substring(0, 30))..." -ForegroundColor Cyan
    
    # 保存Token用于后续测试
    $global:accessToken = $response.data.token
    $global:refreshToken = $response.data.refreshToken
    Write-Host ""
} catch {
    Write-Host "❌ 登录失败: $_" -ForegroundColor Red
    Write-Host ""
}

# 测试4: 获取用户信息
if ($global:accessToken) {
    Write-Host "4. 测试获取用户信息..." -ForegroundColor Yellow
    try {
        $headers = @{
            Authorization = "Bearer $global:accessToken"
        }
        $response = Invoke-RestMethod -Uri "$baseUrl/api/user/info" -Method Get -Headers $headers
        Write-Host "✅ 获取用户信息成功!" -ForegroundColor Green
        Write-Host "用户昵称: $($response.data.nickname)" -ForegroundColor Cyan
        Write-Host "手机号: $($response.data.phone)" -ForegroundColor Cyan
        Write-Host "头像: $($response.data.avatar)" -ForegroundColor Cyan
        Write-Host ""
    } catch {
        Write-Host "❌ 获取用户信息失败: $_" -ForegroundColor Red
        Write-Host ""
    }
}

# 测试5: 邮箱注册
Write-Host "5. 测试邮箱注册..." -ForegroundColor Yellow
$emailRegisterBody = @{
    email = "testuser_$(Get-Random -Maximum 9999)@example.com"
    password = "123456"
    nickname = "测试用户$(Get-Random -Maximum 99)"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/email/register" -Method Post -Body $emailRegisterBody -ContentType "application/json"
    Write-Host "✅ 邮箱注册成功!" -ForegroundColor Green
    Write-Host "用户ID: $($response.data.userId)" -ForegroundColor Cyan
    Write-Host "邮箱: $($response.data.userInfo.email)" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "❌ 邮箱注册失败: $_" -ForegroundColor Red
    Write-Host ""
}

# 测试6: 刷新Token
if ($global:refreshToken) {
    Write-Host "6. 测试刷新Token..." -ForegroundColor Yellow
    $refreshBody = @{
        refreshToken = $global:refreshToken
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/auth/refresh-token" -Method Post -Body $refreshBody -ContentType "application/json"
        Write-Host "✅ Token刷新成功!" -ForegroundColor Green
        Write-Host "新Token: $($response.data.token.Substring(0, 30))..." -ForegroundColor Cyan
        Write-Host ""
    } catch {
        Write-Host "❌ Token刷新失败: $_" -ForegroundColor Red
        Write-Host ""
    }
}

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "测试完成!" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
