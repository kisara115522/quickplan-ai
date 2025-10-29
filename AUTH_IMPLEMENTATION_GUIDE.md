# QuickPlan 用户认证系统 - 实施指南

## 📋 已完成的功能

### ✅ 数据库设计
- **5个核心表**: users, user_third_party, verification_codes, user_tokens, user_login_logs
- **完整字段**: 包含手机号、邮箱、密码、头像、昵称等所有必要字段
- **索引优化**: 为常用查询字段添加了索引
- **软删除支持**: 支持用户数据的软删除

### ✅ 实体类(Entity)
- User - 用户实体
- UserThirdParty - 第三方登录绑定
- VerificationCode - 验证码
- UserToken - 用户Token

### ✅ 数据访问层(Mapper)
- UserMapper - 用户数据操作
- UserThirdPartyMapper - 第三方绑定操作
- VerificationCodeMapper - 验证码操作
- UserTokenMapper - Token操作

### ✅ 业务逻辑层(Service)
- AuthService - 认证服务(注册、登录、Token管理)

### ✅ 接口层(Controller)
- AuthController - 认证接口
  - POST /api/auth/phone/send-code - 发送验证码
  - POST /api/auth/phone/register - 手机号注册
  - POST /api/auth/email/register - 邮箱注册
  - POST /api/auth/phone/login - 手机号登录
  - POST /api/auth/email/login - 邮箱登录
  - POST /api/auth/refresh-token - 刷新Token
  - POST /api/auth/logout - 登出
- UserController - 用户信息接口
  - GET /api/user/info - 获取用户信息

### ✅ 工具类(Util)
- JwtUtil - JWT令牌生成和验证
- PasswordUtil - 密码加密和验证
- IpUtil - IP地址获取

### ✅ 数据传输对象(DTO)
- LoginResponse - 登录响应
- PhoneRegisterRequest - 手机号注册请求
- EmailRegisterRequest - 邮箱注册请求
- PhoneLoginRequest - 手机号登录请求
- EmailLoginRequest - 邮箱登录请求
- SendCodeRequest - 发送验证码请求

### ✅ 依赖配置
- JWT依赖 (jjwt 0.12.3)
- Spring Security Crypto (密码加密)
- Spring Validation (参数校验)
- Apache Commons (工具类)

---

## 🚀 部署步骤

### 1. 执行数据库初始化

```bash
# 在MySQL中执行SQL脚本
mysql -u root -p quickplan_ai < database_auth_schema.sql
```

或者在MySQL客户端中执行 `database_auth_schema.sql` 文件的内容。

### 2. 配置JWT密钥

编辑 `src/main/resources/application.yml`:

```yaml
jwt:
  secret: your-secret-key-change-this-in-production  # ⚠️ 生产环境必须修改
  access-token-expiration: 7200  # 2小时
  refresh-token-expiration: 2592000  # 30天
```

**⚠️ 重要**: 生产环境请使用更安全的密钥!

### 3. 重新编译项目

```powershell
cd c:\Users\18241\Desktop\demo\demo
.\mvnw.cmd clean package -DskipTests
```

### 4. 启动应用

```powershell
.\mvnw.cmd spring-boot:run
```

或者:

```powershell
java -jar target\demo-0.0.1-SNAPSHOT.jar
```

---

## 📝 API使用示例

### 1. 发送验证码

```bash
curl -X POST http://localhost:8080/api/auth/phone/send-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "type": "login"
  }'
```

**开发环境提示**: 验证码会在后端日志中打印出来!

### 2. 手机号注册

```bash
curl -X POST http://localhost:8080/api/auth/phone/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "code": "123456",
    "nickname": "测试用户"
  }'
```

### 3. 手机号登录

```bash
curl -X POST http://localhost:8080/api/auth/phone/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "code": "123456"
  }'
```

### 4. 邮箱注册

```bash
curl -X POST http://localhost:8080/api/auth/email/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "123456",
    "nickname": "邮箱用户"
  }'
```

### 5. 邮箱登录

```bash
curl -X POST http://localhost:8080/api/auth/email/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "123456"
  }'
```

### 6. 获取用户信息(需要Token)

```bash
curl -X GET http://localhost:8080/api/user/info \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 7. 刷新Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
  }'
```

### 8. 登出

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 🔐 安全特性

### 已实现
✅ 密码BCrypt加密存储  
✅ JWT Token认证  
✅ Token自动过期  
✅ 验证码有效期控制  
✅ 验证码发送频率限制(每天10次)  
✅ 验证码60秒发送间隔  
✅ 账号状态检查  
✅ IP地址记录  
✅ 登录日志记录  

### 建议增强
🔄 添加验证码短信服务集成  
🔄 添加图形验证码防止机器人  
🔄 添加登录失败次数限制  
🔄 添加异地登录提醒  
🔄 添加第三方登录(微信、QQ)  
🔄 添加双因素认证  

---

## 🎯 前端集成示例

### Vue.js示例

```javascript
// 登录
async function login() {
  try {
    const response = await fetch('http://localhost:8080/api/auth/phone/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        phone: '13800138000',
        code: '123456'
      })
    });
    
    const result = await response.json();
    
    if (result.success) {
      // 保存Token
      localStorage.setItem('accessToken', result.data.token);
      localStorage.setItem('refreshToken', result.data.refreshToken);
      localStorage.setItem('userInfo', JSON.stringify(result.data.userInfo));
      
      console.log('登录成功', result.data);
    } else {
      console.error('登录失败', result.message);
    }
  } catch (error) {
    console.error('请求失败', error);
  }
}

// 获取用户信息
async function getUserInfo() {
  const token = localStorage.getItem('accessToken');
  
  try {
    const response = await fetch('http://localhost:8080/api/user/info', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    
    if (result.success) {
      console.log('用户信息', result.data);
    } else {
      console.error('获取失败', result.message);
    }
  } catch (error) {
    console.error('请求失败', error);
  }
}
```

---

## ⚠️ 开发环境说明

### 验证码
当前版本中,验证码会直接在后端日志中打印,方便开发调试:

```
发送验证码到手机号: 13800138000, 验证码: 123456 (开发环境)
```

**⚠️ 生产环境需要集成真实的短信服务!**

### 默认头像
系统使用 https://api.dicebear.com 生成随机头像,每个用户根据userId生成唯一头像。

---

## 🧪 测试数据

数据库中已插入测试用户:

```
userId: default_user_001
phone: 13800138000
email: test@example.com
nickname: 测试用户
```

---

## 📊 数据库表说明

### users (用户表)
- 存储用户基本信息
- 支持手机号、邮箱登录
- 密码加密存储
- 包含头像、昵称、性别等个人信息

### user_third_party (第三方登录绑定表)
- 存储微信、QQ等第三方登录信息
- 支持一个账号绑定多个第三方平台

### verification_codes (验证码表)
- 存储所有验证码
- 自动标记过期和已使用
- 支持多种类型(登录、注册、重置密码)

### user_tokens (Token表)
- 存储访问令牌和刷新令牌
- 记录设备信息和IP
- 支持Token失效管理

### user_login_logs (登录日志表)
- 记录所有登录行为
- 包含IP、设备、登录方式等信息
- 用于安全审计

---

## 🛠️ 下一步开发建议

1. **短信服务集成**
   - 集成阿里云短信、腾讯云短信等
   - 修改 `AuthService.sendVerificationCode` 方法

2. **第三方登录**
   - 实现微信登录
   - 实现QQ登录
   - 完善 `UserThirdPartyMapper` 的使用

3. **用户资料管理**
   - 添加修改昵称、头像接口
   - 添加修改密码接口
   - 添加账号绑定/解绑接口

4. **安全增强**
   - 添加Token黑名单
   - 实现登录限流
   - 添加设备管理

5. **数据清理定时任务**
   - 清理过期验证码
   - 清理失效Token
   - 归档旧日志

---

## 📞 技术支持

如有问题,请检查:
1. 数据库是否正确创建
2. application.yml 配置是否正确
3. 后端日志中的错误信息
4. JWT密钥是否配置

祝开发顺利! 🎉
