# 🎯 QuickPlan 用户认证系统

> 完整的用户认证解决方案,包含手机号、邮箱登录,JWT Token管理,数据库设计等全套功能

---

## 📦 项目文件清单

### 📄 数据库
- `database_auth_schema.sql` - 用户认证相关数据库表(5个表)

### 🏗️ 后端代码结构

```
src/main/java/com/example/quickplan_ai/
├── entity/                      # 实体类
│   ├── User.java               # 用户实体
│   ├── UserThirdParty.java     # 第三方登录绑定
│   ├── UserToken.java          # Token管理
│   └── VerificationCode.java   # 验证码
├── mapper/                      # 数据访问层
│   ├── UserMapper.java
│   ├── UserThirdPartyMapper.java
│   ├── UserTokenMapper.java
│   └── VerificationCodeMapper.java
├── Service/                     # 业务逻辑层
│   └── AuthService.java        # 认证服务
├── controller/                  # 接口层
│   ├── AuthController.java     # 认证接口
│   └── UserController.java     # 用户信息接口
├── dto/                         # 数据传输对象
│   ├── LoginResponse.java
│   ├── PhoneRegisterRequest.java
│   ├── EmailRegisterRequest.java
│   ├── PhoneLoginRequest.java
│   ├── EmailLoginRequest.java
│   └── SendCodeRequest.java
├── util/                        # 工具类
│   ├── JwtUtil.java            # JWT工具
│   ├── PasswordUtil.java       # 密码加密
│   └── IpUtil.java             # IP获取
└── config/                      # 配置类
    ├── CorsConfig.java         # 跨域配置
    ├── GlobalExceptionHandler.java  # 全局异常处理
    ├── RequestLoggingInterceptor.java  # 请求日志
    └── WebMvcConfig.java       # Web配置
```

### 📚 文档
- `AUTH_IMPLEMENTATION_GUIDE.md` - 详细实施指南
- `auth_api_guide.md` - API接口文档(原始需求)
- `test_auth_api.ps1` - PowerShell测试脚本

---

## ⚡ 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p quickplan_ai < database_auth_schema.sql
```

### 2. 配置JWT密钥(重要!)

编辑 `src/main/resources/application.yml`:

```yaml
jwt:
  secret: your-secret-key-change-this  # ⚠️ 请修改为随机字符串
  access-token-expiration: 7200
  refresh-token-expiration: 2592000
```

### 3. 编译运行

```powershell
# 编译
.\mvnw.cmd clean package -DskipTests

# 运行
.\mvnw.cmd spring-boot:run
```

### 4. 测试接口

```powershell
# 运行测试脚本
.\test_auth_api.ps1
```

或手动测试:

```bash
# 发送验证码
curl -X POST http://localhost:8080/api/auth/phone/send-code \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","type":"login"}'

# 手机号登录(验证码在后端日志中)
curl -X POST http://localhost:8080/api/auth/phone/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","code":"123456"}'
```

---

## 🔥 核心功能

### ✅ 用户注册
- ✅ 手机号+验证码注册
- ✅ 邮箱+密码注册
- ✅ 自动生成昵称和头像
- ✅ 密码BCrypt加密

### ✅ 用户登录
- ✅ 手机号+验证码登录
- ✅ 邮箱+密码登录
- ✅ 自动注册(手机号登录)
- ✅ 登录信息记录

### ✅ Token管理
- ✅ JWT Token生成
- ✅ 访问令牌(2小时)
- ✅ 刷新令牌(30天)
- ✅ Token验证
- ✅ Token刷新
- ✅ 登出(Token失效)

### ✅ 验证码系统
- ✅ 6位数字验证码
- ✅ 5分钟有效期
- ✅ 发送频率限制(每天10次,60秒间隔)
- ✅ 验证码自动失效
- ✅ 开发环境日志打印

### ✅ 安全特性
- ✅ 密码加密存储(BCrypt)
- ✅ JWT令牌认证
- ✅ Token过期管理
- ✅ IP地址记录
- ✅ 登录日志
- ✅ 账号状态检查

### ✅ 数据库设计
- ✅ 5个核心表
- ✅ 完整字段(手机号、邮箱、密码、头像等)
- ✅ 索引优化
- ✅ 软删除支持
- ✅ 第三方登录预留

---

## 🌐 API接口列表

| 接口 | 方法 | 说明 | 认证 |
|-----|------|------|------|
| /api/auth/phone/send-code | POST | 发送验证码 | ❌ |
| /api/auth/phone/register | POST | 手机号注册 | ❌ |
| /api/auth/email/register | POST | 邮箱注册 | ❌ |
| /api/auth/phone/login | POST | 手机号登录 | ❌ |
| /api/auth/email/login | POST | 邮箱登录 | ❌ |
| /api/auth/refresh-token | POST | 刷新Token | ❌ |
| /api/auth/logout | POST | 登出 | ✅ |
| /api/user/info | GET | 获取用户信息 | ✅ |

---

## 📊 数据库表结构

### 1. users (用户表)
存储用户基本信息,包含手机号、邮箱、密码、头像、昵称等

### 2. user_third_party (第三方登录)
存储微信、QQ等第三方登录绑定信息

### 3. verification_codes (验证码)
存储所有验证码,支持多种类型

### 4. user_tokens (Token管理)
存储访问令牌和刷新令牌

### 5. user_login_logs (登录日志)
记录所有登录行为,用于安全审计

---

## 🔧 配置说明

### JWT配置

```yaml
jwt:
  secret: your-secret-key          # JWT密钥(必须修改!)
  access-token-expiration: 7200    # 访问令牌有效期(秒)
  refresh-token-expiration: 2592000 # 刷新令牌有效期(秒)
```

### 验证码配置

```yaml
verification:
  code:
    length: 6              # 验证码长度
    expiration: 300        # 有效期(秒)
    daily-limit: 10        # 每天发送限制
    interval: 60           # 发送间隔(秒)
```

---

## 🎨 前端集成

### 登录流程

```javascript
// 1. 发送验证码
await fetch('/api/auth/phone/send-code', {
  method: 'POST',
  body: JSON.stringify({ phone: '13800138000', type: 'login' })
});

// 2. 手机号登录
const response = await fetch('/api/auth/phone/login', {
  method: 'POST',
  body: JSON.stringify({ phone: '13800138000', code: '123456' })
});

const { data } = await response.json();
// 保存Token
localStorage.setItem('token', data.token);
localStorage.setItem('refreshToken', data.refreshToken);

// 3. 带Token请求
await fetch('/api/user/info', {
  headers: {
    'Authorization': `Bearer ${data.token}`
  }
});
```

---

## 💡 开发提示

### 验证码测试
开发环境下,验证码会在后端日志中打印:

```
发送验证码到手机号: 13800138000, 验证码: 123456 (开发环境)
```

⚠️ 生产环境需要集成真实短信服务!

### 默认头像
使用 DiceBear API 生成随机头像:
```
https://api.dicebear.com/7.x/avataaars/svg?seed={userId}
```

### Token使用
- Access Token: 用于API请求,有效期2小时
- Refresh Token: 用于刷新Access Token,有效期30天
- 建议在Access Token过期前主动刷新

---

## 🚀 下一步开发

### 待实现功能
- [ ] 短信服务集成(阿里云/腾讯云)
- [ ] 第三方登录(微信/QQ)
- [ ] 修改密码
- [ ] 找回密码
- [ ] 修改用户资料
- [ ] 账号绑定/解绑
- [ ] 登录设备管理
- [ ] 异地登录提醒
- [ ] 双因素认证

### 优化建议
- [ ] Token黑名单机制
- [ ] 登录限流
- [ ] 图形验证码
- [ ] 人机验证
- [ ] 数据清理定时任务

---

## 📝 依赖项

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- 密码加密 -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>

<!-- 参数校验 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 🐛 常见问题

### Q: 验证码收不到?
A: 开发环境验证码在后端日志中打印,生产环境需要集成短信服务

### Q: Token验证失败?
A: 检查请求头格式: `Authorization: Bearer {token}`

### Q: 数据库连接失败?
A: 检查 `application.yml` 中的数据库配置

### Q: JWT密钥错误?
A: 确保 `application.yml` 中配置了 `jwt.secret`

---

## 📖 相关文档

- [AUTH_IMPLEMENTATION_GUIDE.md](./AUTH_IMPLEMENTATION_GUIDE.md) - 详细实施指南
- [auth_api_guide.md](./docs/auth_api_guide.md) - API接口规范
- [database_auth_schema.sql](./database_auth_schema.sql) - 数据库脚本

---

## 📄 License

MIT License

---

## 👨‍💻 技术栈

- Spring Boot 3.5.7
- MyBatis 3.0.3
- MySQL 8.0+
- JWT (jjwt 0.12.3)
- BCrypt密码加密
- Lombok
- Jakarta Validation

---

**✨ 功能完整,开箱即用! ✨**

如有问题,请查看详细文档或检查后端日志 🔍
