# QuickPlan AI - API 接口文档

## 项目简介
这是一个类似ChatGPT网页端的AI对话助手,支持:
- 查看历史会话列表
- 查看同一会话的历史记录
- 创建新对话
- 流式AI回复

## 数据库配置

### 1. 执行建表SQL
在MySQL中执行 `src/main/resources/schema.sql` 文件来创建数据库和表结构。

### 2. 配置数据库连接
修改 `application.yml` 中的数据库配置:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quickplan_ai
    username: root
    password: 你的密码
```

## API 接口说明

### 基础URL
```
http://localhost:8080
```

---

## 1. 会话管理接口 (`/api/conversation`)

### 1.1 创建新会话
**POST** `/api/conversation/create`

**请求体:**
```json
{
  "userId": "user-001",
  "title": "新对话"
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "id": "uuid-123456",
    "userId": "user-001",
    "title": "新对话",
    "createdAt": "2025-10-24T10:00:00",
    "updatedAt": "2025-10-24T10:00:00",
    "isDeleted": 0
  }
}
```

---

### 1.2 获取用户的所有会话列表
**GET** `/api/conversation/list/{userId}`

**请求示例:**
```
GET /api/conversation/list/user-001
```

**响应:**
```json
{
  "success": true,
  "data": [
    {
      "id": "conversation-001",
      "userId": "user-001",
      "title": "如何学习Spring Boot?",
      "createdAt": "2025-10-24T10:00:00",
      "updatedAt": "2025-10-24T11:30:00",
      "isDeleted": 0
    }
  ],
  "total": 1
}
```

---

### 1.3 获取最近的N个会话
**GET** `/api/conversation/recent/{userId}?limit=10`

**请求示例:**
```
GET /api/conversation/recent/user-001?limit=5
```

**响应:**
```json
{
  "success": true,
  "data": [...]
}
```

---

### 1.4 获取会话详情
**GET** `/api/conversation/detail/{conversationId}`

**请求示例:**
```
GET /api/conversation/detail/conversation-001
```

**响应:**
```json
{
  "success": true,
  "data": {
    "id": "conversation-001",
    "userId": "user-001",
    "title": "如何学习Spring Boot?",
    "createdAt": "2025-10-24T10:00:00",
    "updatedAt": "2025-10-24T11:30:00",
    "isDeleted": 0
  }
}
```

---

### 1.5 获取会话的历史消息
**GET** `/api/conversation/messages/{conversationId}`

**请求示例:**
```
GET /api/conversation/messages/conversation-001
```

**响应:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "conversationId": "conversation-001",
      "role": "user",
      "content": "你好,请问如何使用Spring Boot?",
      "createdAt": "2025-10-24T10:00:00",
      "isDeleted": 0
    },
    {
      "id": 2,
      "conversationId": "conversation-001",
      "role": "assistant",
      "content": "你好!使用Spring Boot很简单...",
      "createdAt": "2025-10-24T10:00:05",
      "isDeleted": 0
    }
  ],
  "total": 2
}
```

---

### 1.6 更新会话标题
**PUT** `/api/conversation/update-title`

**请求体:**
```json
{
  "conversationId": "conversation-001",
  "title": "Spring Boot学习笔记"
}
```

**响应:**
```json
{
  "success": true,
  "message": "更新成功"
}
```

---

### 1.7 删除会话
**DELETE** `/api/conversation/delete/{conversationId}`

**请求示例:**
```
DELETE /api/conversation/delete/conversation-001
```

**响应:**
```json
{
  "success": true,
  "message": "删除成功"
}
```

---

### 1.8 获取会话统计信息
**GET** `/api/conversation/stats/{conversationId}`

**请求示例:**
```
GET /api/conversation/stats/conversation-001
```

**响应:**
```json
{
  "success": true,
  "data": {
    "conversation": {...},
    "messageCount": 10
  }
}
```

---

## 2. AI对话接口 (`/api/ai`)

### 2.1 发送消息并获取AI回复(流式)
**POST** `/api/ai/chat`

**请求体:**
```json
{
  "memoryId": "conversation-001",
  "userId": "user-001",
  "message": "什么是Spring Boot?"
}
```

**响应:**
流式返回(Server-Sent Events):
```
Spring
 Boot
 是
 一个
 ...
```

**说明:**
- 该接口会自动保存用户消息和AI回复到数据库
- 如果是会话的第一条消息,会自动生成会话标题(取用户消息前30个字符)
- 使用流式响应,前端可以实时显示AI的回复

---

### 2.2 创建新会话
**POST** `/api/ai/chat/new`

**请求体:**
```json
{
  "userId": "user-001"
}
```

**响应:**
```json
{
  "success": true,
  "data": {
    "id": "uuid-new-conversation",
    "userId": "user-001",
    "title": "新对话",
    "createdAt": "2025-10-24T12:00:00",
    "updatedAt": "2025-10-24T12:00:00",
    "isDeleted": 0
  },
  "message": "新会话创建成功"
}
```

---

## 前端使用示例

### 使用fetch进行流式请求
```javascript
// 发送消息并接收流式响应
async function sendMessage(conversationId, message) {
  const response = await fetch('http://localhost:8080/api/ai/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      memoryId: conversationId,
      userId: 'user-001',
      message: message
    })
  });

  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    
    const chunk = decoder.decode(value);
    console.log('收到AI回复:', chunk);
    // 在页面上显示chunk
  }
}

// 获取会话列表
async function getConversations(userId) {
  const response = await fetch(`http://localhost:8080/api/conversation/list/${userId}`);
  const data = await response.json();
  console.log('会话列表:', data.data);
}

// 获取会话历史消息
async function getMessages(conversationId) {
  const response = await fetch(`http://localhost:8080/api/conversation/messages/${conversationId}`);
  const data = await response.json();
  console.log('历史消息:', data.data);
}
```

---

## 数据模型

### Conversation (会话)
```java
{
  id: String,           // 会话UUID
  userId: String,       // 用户ID
  title: String,        // 会话标题
  createdAt: DateTime,  // 创建时间
  updatedAt: DateTime,  // 更新时间
  isDeleted: Integer    // 逻辑删除标识
}
```

### Message (消息)
```java
{
  id: Long,                 // 消息ID(自增)
  conversationId: String,   // 所属会话ID
  role: String,            // "user" 或 "assistant"
  content: String,         // 消息内容
  createdAt: DateTime,     // 创建时间
  isDeleted: Integer       // 逻辑删除标识
}
```

---

## 启动步骤

1. **配置数据库**
   - 确保MySQL服务运行
   - 执行 `schema.sql` 创建数据库和表

2. **配置环境变量**
   ```bash
   # Windows PowerShell
   $env:SILICON_API_KEY="your-api-key"
   $env:MYSQL_PASSWORD="your-mysql-password"
   
   # Linux/Mac
   export SILICON_API_KEY="your-api-key"
   export MYSQL_PASSWORD="your-mysql-password"
   ```

3. **启动Redis**
   ```bash
   redis-server
   ```

4. **运行项目**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **测试接口**
   使用Postman或curl测试上述接口

---

## 注意事项

1. **会话ID (memoryId)**: 在调用 `/api/ai/chat` 之前,需要先创建会话或使用已有的会话ID
2. **用户ID**: 实际项目中应该从用户认证系统获取,这里使用固定值做演示
3. **逻辑删除**: 删除操作是逻辑删除,数据仍保留在数据库中
4. **流式响应**: AI回复使用流式传输,适合前端实时显示

---

## 技术栈

- Spring Boot 3.5.7
- MyBatis-Plus 3.5.5
- MySQL 8.0+
- Redis
- LangChain4j
- Lombok
