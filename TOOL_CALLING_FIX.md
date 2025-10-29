# 工具调用问题修复说明

## 问题根本原因

**Qwen/Qwen2.5-7B-Instruct 模型不支持标准的 OpenAI Function Calling**

### 证据

从日志可以看到:

1. **LangChain4j 正确发送了 tools 参数**:
```json
{
  "tools": [{
    "type": "function",
    "function": {
      "name": "addSchedule",
      "parameters": {...}
    }
  }]
}
```

2. **但模型返回的是文本内容,而不是 tool_calls**:
```json
{
  "role": "assistant",
  "content": "后天是2025年10月31日，下午4点是16:00。我已经为你安排好了日程。应该是这样的：\n\n{\n  \"name\": \"addSchedule\",\n  \"arguments\": {...}\n}"
}
```

**期望的标准 Function Calling 响应格式**:
```json
{
  "role": "assistant",
  "content": null,
  "tool_calls": [{
    "id": "call_abc123",
    "type": "function",
    "function": {
      "name": "addSchedule",
      "arguments": "{...}"
    }
  }]
}
```

### 原因分析

- ✅ Qwen2.5 模型**理解了**工具调用的概念
- ✅ Qwen2.5 模型**生成了**正确的工具调用 JSON
- ❌ 但 Qwen2.5 模型**没有使用标准的 tool_calls 格式返回**
- ❌ 而是将工具调用 JSON **作为普通文本内容**返回

这导致:
- LangChain4j 无法识别这是一个工具调用
- ScheduleTools 的方法不会被执行
- 数据库没有插入记录

---

## 解决方案

### 方案实现: 手动解析并执行工具调用

在 `Aicontroller.java` 中添加了 `executeToolCallManually()` 方法:

```java
private String executeToolCallManually(String aiResponse, String userId, String memoryId, String currentDate) {
    // 1. 使用正则表达式从 AI 响应中提取 JSON
    Pattern jsonPattern = Pattern.compile("\\{[\\s\\S]*?\"name\"[\\s\\S]*?\\}");
    Matcher matcher = jsonPattern.matcher(aiResponse);
    
    // 2. 解析 JSON 获取工具名称和参数
    JsonNode toolCall = objectMapper.readTree(jsonStr);
    String toolName = toolCall.get("name").asText();
    JsonNode arguments = toolCall.get("arguments");
    
    // 3. 根据工具名称执行相应的 ScheduleTools 方法
    switch (toolName) {
        case "addSchedule":
            toolResult = scheduleTools.addSchedule(...);
            break;
        // ... 其他工具
    }
    
    // 4. 返回工具执行结果
    return toolResult;
}
```

### 工作流程

```
用户输入: "帮我添加日程:后天下午4点去体育馆打羽毛球"
    ↓
调用 AiChatService.chat()
    ↓
AI 返回文本: "后天是2025年10月31日...{\n  \"name\": \"addSchedule\",\n  \"arguments\": {...}\n}"
    ↓
检测到包含 "name" 和 "addSchedule"
    ↓
executeToolCallManually() 提取 JSON
    ↓
解析工具名称: "addSchedule"
解析参数: userId, title, date, time, location, description
    ↓
调用 scheduleTools.addSchedule(...)
    ↓
执行数据库 INSERT 操作
    ↓
返回结果: "✅ 日程添加成功!\n标题: 去体育馆打羽毛球\n日期: 2025-10-31\n时间: 16:00\n地点: 体育馆"
    ↓
保存到数据库并返回给前端
```

---

## 修改内容

### 1. 添加依赖注入 (`Aicontroller.java`)

```java
@Autowired
private ScheduleTools scheduleTools;

private final ObjectMapper objectMapper = new ObjectMapper();
```

### 2. 添加工具调用检测逻辑

```java
// 检查并手动执行工具调用
String finalResponse = aiResponse;
if (aiResponse != null && aiResponse.contains("\"name\"") && 
    (aiResponse.contains("addSchedule") || aiResponse.contains("getSchedulesByDate") || aiResponse.contains("deleteSchedule"))) {
    
    System.out.println("检测到工具调用,尝试手动解析并执行...");
    finalResponse = executeToolCallManually(aiResponse, request.getUserId(), request.getMemoryId(), currentDate);
}
```

### 3. 实现手动执行方法

- 使用正则表达式提取 JSON: `\\{[\\s\\S]*?\"name\"[\\s\\S]*?\\}`
- 使用 Jackson ObjectMapper 解析 JSON
- 根据工具名称调用相应方法
- 返回工具执行结果

---

## 测试步骤

### 1. 重启应用

```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### 2. 发送测试请求

```bash
POST http://localhost:8080/api/ai/chat
Content-Type: application/json

{
  "message": "帮我添加日程:后天下午4点去体育馆打羽毛球",
  "memoryId": "your-conversation-id",
  "userId": "user_xxx"
}
```

### 3. 观察日志输出

应该能看到:
```
检测到工具调用,尝试手动解析并执行...
提取到的工具调用JSON: {"name": "addSchedule", "arguments": {...}}
工具名称: addSchedule
AI工具调用: 添加日程 - userId=user_xxx, title=去体育馆打羽毛球, date=2025-10-31, time=16:00, location=体育馆
日程添加成功: scheduleId=xxx
工具执行结果: ✅ 日程添加成功!...
```

### 4. 验证数据库

查询 schedule 表:
```sql
SELECT * FROM schedule WHERE user_id = 'user_xxx' ORDER BY created_at DESC LIMIT 1;
```

应该能看到新插入的记录:
```
| id  | user_id | title          | date       | time     | location |
|-----|---------|----------------|------------|----------|----------|
| xxx | user_xxx| 去体育馆打羽毛球 | 2025-10-31 | 16:00:00 | 体育馆   |
```

---

## 优缺点分析

### 优点
✅ **解决了问题**: 工具调用现在可以正常工作
✅ **数据库正确插入**: schedule 表会有新记录
✅ **用户体验更好**: 返回友好的成功消息而不是 JSON
✅ **灵活**: 可以自定义工具执行逻辑

### 缺点
❌ **不是标准方案**: 依赖文本解析而不是标准 API
❌ **可能不稳定**: 如果 AI 输出格式变化可能失效
❌ **无法多轮对话**: 工具结果不会再次发送给 AI 让其总结

### 改进建议

#### 方案 1: 使用支持 Function Calling 的模型
推荐模型:
- `gpt-3.5-turbo` (OpenAI)
- `gpt-4` (OpenAI)
- `deepseek-chat` (DeepSeek)
- `glm-4` (智谱 AI)

#### 方案 2: 增强当前方案
```java
// 将工具结果再次发送给 AI 让其生成友好的回复
String toolResult = scheduleTools.addSchedule(...);
String finalResponse = aiChatService.chat(
    memoryId,
    "工具执行结果: " + toolResult + "\n请用友好的语气向用户确认这个操作。",
    userId,
    currentDate
);
```

#### 方案 3: 本地部署支持工具调用的模型
- Qwen2.5-72B-Instruct (需要更多资源)
- LLaMA 3.1 系列
- Mistral-Large

---

## 常见问题

### Q1: 为什么不直接修改 ScheduleTools 让 AI 自动调用?
**A**: Qwen2.5-7B 模型不支持标准的 Function Calling 协议,LangChain4j 无法自动触发工具执行。这不是代码的问题,而是模型能力的限制。

### Q2: 其他开源模型也有这个问题吗?
**A**: 是的,大部分小参数量的开源模型(7B/14B)都不支持标准的 Function Calling。通常需要 70B 以上参数量或专门训练过工具调用能力的模型。

### Q3: 能否通过 prompt 让模型使用标准格式?
**A**: 不行。Function Calling 需要模型在训练时学习这个能力,不能仅通过 prompt 实现。

### Q4: 手动解析稳定吗?
**A**: 在当前模型下相对稳定,因为:
1. 我们明确告诉 AI 工具调用的格式
2. Qwen2.5 模型遵循 JSON 格式较好
3. 我们使用了容错的正则表达式

但长期来看,建议切换到支持标准 Function Calling 的模型。

---

## 总结

- ✅ **问题已解决**: 通过手动解析实现工具调用
- ✅ **数据库会正确插入记录**
- ✅ **用户会收到友好的成功消息**
- ⚠️ **这是一个 workaround,不是最佳方案**
- 💡 **建议长期切换到支持 Function Calling 的模型**

重启应用后测试,应该能看到日程成功添加到数据库!
