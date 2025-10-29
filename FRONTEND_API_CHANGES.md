# 前端API接口变更说明

## 修改内容
已将所有接口从**流式调用**改为**非流式调用**,并删除了流式相关依赖。

---

## 后端返回的数据类型

### 1. `/api/ai/chat` (AI对话接口)

**之前 (流式)**:
- 返回类型: `text/event-stream` (SSE 流)
- 数据格式: 分块返回字符串流

**现在 (非流式)**:
- 返回类型: `application/json`
- HTTP状态码: `200 OK`
- 数据格式:
```json
{
  "success": true,           // boolean - 是否成功
  "message": "AI的完整回复内容",  // string - AI回复的完整文本
  "data": null               // object - 当前为null,预留扩展字段
}
```

**错误响应示例**:
```json
{
  "success": false,
  "message": "AI服务连接中断，请重试",
  "data": null
}
```

---

### 2. `/api/ai/chat/new` (创建新会话)

**保持不变**:
- 返回类型: `application/json`
- 数据格式:
```json
{
  "success": true,
  "message": "创建成功",
  "data": {
    "conversationId": "uuid-string",
    "userId": "user_xxx",
    "title": "新对话",
    "createdAt": "2025-10-29T10:30:00",
    "updatedAt": "2025-10-29T10:30:00"
  }
}
```

---

## 前端需要修改的地方

### 1. **移除 EventSource 或 fetch stream 处理**

**之前的代码可能是这样**:
```javascript
// ❌ 旧代码 - 使用 EventSource 或 fetch stream
const eventSource = new EventSource('/api/ai/chat');
eventSource.onmessage = (event) => {
  const chunk = event.data;
  // 逐块处理数据
  displayMessage(chunk);
};

// 或者使用 fetch stream
const response = await fetch('/api/ai/chat', {
  method: 'POST',
  body: JSON.stringify(request)
});
const reader = response.body.getReader();
const decoder = new TextDecoder();
while (true) {
  const { done, value } = await reader.read();
  if (done) break;
  const chunk = decoder.decode(value);
  displayMessage(chunk);
}
```

**现在改为普通 fetch**:
```javascript
// ✅ 新代码 - 使用普通 fetch
const response = await fetch('/api/ai/chat', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    message: userMessage,
    memoryId: conversationId,
    userId: currentUserId
  })
});

const result = await response.json();

if (result.success) {
  // 显示完整的AI回复
  displayMessage(result.message);
} else {
  // 显示错误信息
  showError(result.message);
}
```

---

### 2. **使用 axios (如果你用 axios)**

```javascript
// ✅ 使用 axios
try {
  const response = await axios.post('/api/ai/chat', {
    message: userMessage,
    memoryId: conversationId,
    userId: currentUserId
  });

  if (response.data.success) {
    displayMessage(response.data.message);
  } else {
    showError(response.data.message);
  }
} catch (error) {
  console.error('请求失败:', error);
  showError('网络错误，请稍后重试');
}
```

---

### 3. **处理打字机效果 (可选)**

如果你想保留流式的打字机效果,可以在前端模拟:

```javascript
// ✅ 前端模拟打字机效果
async function sendMessage(userMessage) {
  const response = await fetch('/api/ai/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      message: userMessage,
      memoryId: conversationId,
      userId: currentUserId
    })
  });

  const result = await response.json();

  if (result.success) {
    // 模拟逐字显示
    const fullText = result.message;
    let displayedText = '';
    
    for (let i = 0; i < fullText.length; i++) {
      displayedText += fullText[i];
      updateMessageDisplay(displayedText);
      await sleep(50); // 每个字符延迟50ms
    }
  } else {
    showError(result.message);
  }
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
```

---

### 4. **加载状态处理**

```javascript
// ✅ 添加加载状态
async function sendMessage(userMessage) {
  // 显示加载中
  showLoading(true);
  
  try {
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        message: userMessage,
        memoryId: conversationId,
        userId: currentUserId
      })
    });

    const result = await response.json();

    if (result.success) {
      displayMessage(result.message);
    } else {
      showError(result.message);
    }
  } catch (error) {
    showError('网络错误，请稍后重试');
  } finally {
    // 隐藏加载状态
    showLoading(false);
  }
}
```

---

## 完整示例 (Vue 3)

```vue
<template>
  <div class="chat-container">
    <div class="messages">
      <div v-for="msg in messages" :key="msg.id" :class="msg.role">
        {{ msg.content }}
      </div>
      <div v-if="loading" class="loading">AI思考中...</div>
    </div>
    
    <div class="input-area">
      <input v-model="userInput" @keyup.enter="sendMessage" />
      <button @click="sendMessage" :disabled="loading">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const messages = ref([]);
const userInput = ref('');
const loading = ref(false);
const conversationId = ref('your-conversation-id');
const userId = ref('your-user-id');

async function sendMessage() {
  if (!userInput.value.trim() || loading.value) return;

  const userMessage = userInput.value;
  
  // 添加用户消息到界面
  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: userMessage
  });
  
  userInput.value = '';
  loading.value = true;

  try {
    const response = await axios.post('/api/ai/chat', {
      message: userMessage,
      memoryId: conversationId.value,
      userId: userId.value
    });

    if (response.data.success) {
      // 添加AI回复到界面
      messages.value.push({
        id: Date.now(),
        role: 'assistant',
        content: response.data.message
      });
    } else {
      alert(response.data.message);
    }
  } catch (error) {
    console.error('发送失败:', error);
    alert('发送失败，请重试');
  } finally {
    loading.value = false;
  }
}
</script>
```

---

## 完整示例 (React)

```jsx
import React, { useState } from 'react';
import axios from 'axios';

function ChatComponent() {
  const [messages, setMessages] = useState([]);
  const [userInput, setUserInput] = useState('');
  const [loading, setLoading] = useState(false);
  const conversationId = 'your-conversation-id';
  const userId = 'your-user-id';

  const sendMessage = async () => {
    if (!userInput.trim() || loading) return;

    const userMessage = userInput;

    // 添加用户消息
    setMessages(prev => [...prev, {
      id: Date.now(),
      role: 'user',
      content: userMessage
    }]);

    setUserInput('');
    setLoading(true);

    try {
      const response = await axios.post('/api/ai/chat', {
        message: userMessage,
        memoryId: conversationId,
        userId: userId
      });

      if (response.data.success) {
        // 添加AI回复
        setMessages(prev => [...prev, {
          id: Date.now(),
          role: 'assistant',
          content: response.data.message
        }]);
      } else {
        alert(response.data.message);
      }
    } catch (error) {
      console.error('发送失败:', error);
      alert('发送失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map(msg => (
          <div key={msg.id} className={msg.role}>
            {msg.content}
          </div>
        ))}
        {loading && <div className="loading">AI思考中...</div>}
      </div>
      
      <div className="input-area">
        <input
          value={userInput}
          onChange={(e) => setUserInput(e.target.value)}
          onKeyUp={(e) => e.key === 'Enter' && sendMessage()}
        />
        <button onClick={sendMessage} disabled={loading}>
          发送
        </button>
      </div>
    </div>
  );
}

export default ChatComponent;
```

---

## 总结

### 后端返回的数据类型:
1. **`/api/ai/chat`**: 返回 `Map<String, Object>` → JSON 格式
   ```json
   {
     "success": boolean,
     "message": string,
     "data": null
   }
   ```

2. **`/api/ai/chat/new`**: 返回 `Map<String, Object>` → JSON 格式
   ```json
   {
     "success": boolean,
     "message": string,
     "data": Conversation对象
   }
   ```

### 前端主要修改:
1. ❌ 删除 `EventSource`、`fetch stream`、`ReadableStream` 相关代码
2. ✅ 改用普通的 `fetch()` 或 `axios.post()`
3. ✅ 处理返回的 JSON 对象: `response.data.success` 和 `response.data.message`
4. ✅ 如需打字机效果,在前端用 `setTimeout` 模拟
5. ✅ 添加 loading 状态提升用户体验

### 优势:
- ✅ **更好的错误处理**: 统一的成功/失败状态
- ✅ **支持工具调用**: 非流式才能执行 function calling
- ✅ **更简单**: 前端代码更简洁,无需处理流
- ✅ **更稳定**: 避免流断连、超时等问题
