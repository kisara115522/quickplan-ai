# 日程管理 API 接口文档

## 基础信息

- **Base URL**: `/api/schedule`
- **跨域支持**: 已开启 CORS (`@CrossOrigin`)
- **数据格式**: JSON
- **字符编码**: UTF-8

---

## 接口列表

### 1. 创建日程

创建一个新的日程安排。

#### 请求信息

- **URL**: `/api/schedule/create`
- **Method**: `POST`
- **Content-Type**: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | String | ✅ | 用户ID | `"user123"` |
| title | String | ✅ | 日程标题 | `"团队会议"` |
| date | String | ✅ | 日期 (yyyy-MM-dd) | `"2025-10-28"` |
| time | String | ❌ | 时间 (HH:mm:ss) | `"14:30:00"` |
| location | String | ❌ | 地点 | `"会议室A"` |
| description | String | ❌ | 备注描述 | `"讨论Q4规划"` |

#### 请求示例

```json
{
  "userId": "user123",
  "title": "团队会议",
  "location": "会议室A",
  "date": "2025-10-28",
  "time": "14:30:00",
  "description": "讨论Q4规划"
}
```

#### 响应示例

**成功响应 (200 OK)**:
```json
{
  "success": true,
  "message": "创建成功",
  "data": {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "userId": "user123",
    "title": "团队会议",
    "location": "会议室A",
    "date": "2025-10-28",
    "time": "14:30:00",
    "description": "讨论Q4规划",
    "createdAt": "2025-10-27T18:30:00",
    "updatedAt": "2025-10-27T18:30:00",
    "isDeleted": 0
  }
}
```

**失败响应 (400 Bad Request)**:
```json
{
  "success": false,
  "message": "用户ID不能为空",
  "data": null
}
```

---

### 2. 获取用户所有日程

获取指定用户的所有日程列表（按日期和时间升序排列）。

#### 请求信息

- **URL**: `/api/schedule/list/{userId}`
- **Method**: `GET`

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | String | ✅ | 用户ID | `"user123"` |

#### 请求示例

```http
GET /api/schedule/list/user123
```

#### 响应示例

```json
{
  "success": true,
  "message": null,
  "total": 2,
  "data": [
    {
      "id": "schedule-id-001",
      "userId": "user123",
      "title": "团队会议",
      "location": "会议室A",
      "date": "2025-10-28",
      "time": "14:30:00",
      "description": "讨论Q4规划",
      "createdAt": "2025-10-27T18:30:00",
      "updatedAt": "2025-10-27T18:30:00",
      "isDeleted": 0
    },
    {
      "id": "schedule-id-002",
      "userId": "user123",
      "title": "客户拜访",
      "location": "客户公司",
      "date": "2025-10-29",
      "time": "10:00:00",
      "description": "产品演示",
      "createdAt": "2025-10-27T19:00:00",
      "updatedAt": "2025-10-27T19:00:00",
      "isDeleted": 0
    }
  ]
}
```

---

### 3. 获取指定日期的日程

获取用户在某个具体日期的所有日程。

#### 请求信息

- **URL**: `/api/schedule/date`
- **Method**: `GET`

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | String | ✅ | 用户ID | `"user123"` |
| date | String | ✅ | 日期 (yyyy-MM-dd) | `"2025-10-28"` |

#### 请求示例

```http
GET /api/schedule/date?userId=user123&date=2025-10-28
```

#### 响应示例

```json
{
  "success": true,
  "message": null,
  "total": 1,
  "data": [
    {
      "id": "schedule-id-001",
      "userId": "user123",
      "title": "团队会议",
      "location": "会议室A",
      "date": "2025-10-28",
      "time": "14:30:00",
      "description": "讨论Q4规划",
      "createdAt": "2025-10-27T18:30:00",
      "updatedAt": "2025-10-27T18:30:00",
      "isDeleted": 0
    }
  ]
}
```

---

### 4. 获取日期范围内的日程

获取用户在指定日期范围内的所有日程（常用于周视图、月视图）。

#### 请求信息

- **URL**: `/api/schedule/range`
- **Method**: `GET`

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| userId | String | ✅ | 用户ID | `"user123"` |
| startDate | String | ✅ | 开始日期 (yyyy-MM-dd) | `"2025-10-27"` |
| endDate | String | ✅ | 结束日期 (yyyy-MM-dd) | `"2025-11-02"` |

#### 请求示例

```http
GET /api/schedule/range?userId=user123&startDate=2025-10-27&endDate=2025-11-02
```

#### 响应示例

```json
{
  "success": true,
  "message": null,
  "total": 3,
  "data": [
    {
      "id": "schedule-id-001",
      "userId": "user123",
      "title": "团队会议",
      "location": "会议室A",
      "date": "2025-10-28",
      "time": "14:30:00",
      "description": "讨论Q4规划",
      "createdAt": "2025-10-27T18:30:00",
      "updatedAt": "2025-10-27T18:30:00",
      "isDeleted": 0
    },
    {
      "id": "schedule-id-002",
      "userId": "user123",
      "title": "客户拜访",
      "location": "客户公司",
      "date": "2025-10-29",
      "time": "10:00:00",
      "description": "产品演示",
      "createdAt": "2025-10-27T19:00:00",
      "updatedAt": "2025-10-27T19:00:00",
      "isDeleted": 0
    },
    {
      "id": "schedule-id-003",
      "userId": "user123",
      "title": "项目评审",
      "location": "线上会议",
      "date": "2025-10-30",
      "time": "16:00:00",
      "description": "技术方案评审",
      "createdAt": "2025-10-27T20:00:00",
      "updatedAt": "2025-10-27T20:00:00",
      "isDeleted": 0
    }
  ]
}
```

---

### 5. 获取日程详情

根据日程ID获取单个日程的详细信息。

#### 请求信息

- **URL**: `/api/schedule/detail/{scheduleId}`
- **Method**: `GET`

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| scheduleId | String | ✅ | 日程ID | `"schedule-id-001"` |

#### 请求示例

```http
GET /api/schedule/detail/schedule-id-001
```

#### 响应示例

**成功响应**:
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "schedule-id-001",
    "userId": "user123",
    "title": "团队会议",
    "location": "会议室A",
    "date": "2025-10-28",
    "time": "14:30:00",
    "description": "讨论Q4规划",
    "createdAt": "2025-10-27T18:30:00",
    "updatedAt": "2025-10-27T18:30:00",
    "isDeleted": 0
  }
}
```

**日程不存在**:
```json
{
  "success": false,
  "message": "日程不存在",
  "data": null
}
```

---

### 6. 更新日程

更新已存在的日程信息。

#### 请求信息

- **URL**: `/api/schedule/update`
- **Method**: `PUT`
- **Content-Type**: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | String | ✅ | 日程ID | `"schedule-id-001"` |
| title | String | ❌ | 日程标题 | `"更新后的会议"` |
| date | String | ❌ | 日期 (yyyy-MM-dd) | `"2025-10-29"` |
| time | String | ❌ | 时间 (HH:mm:ss) | `"15:00:00"` |
| location | String | ❌ | 地点 | `"会议室B"` |
| description | String | ❌ | 备注描述 | `"更新后的描述"` |

#### 请求示例

```json
{
  "id": "schedule-id-001",
  "title": "更新后的团队会议",
  "location": "会议室B",
  "date": "2025-10-29",
  "time": "15:00:00",
  "description": "更新议题"
}
```

#### 响应示例

**成功响应**:
```json
{
  "success": true,
  "message": "更新成功",
  "data": {
    "id": "schedule-id-001",
    "userId": "user123",
    "title": "更新后的团队会议",
    "location": "会议室B",
    "date": "2025-10-29",
    "time": "15:00:00",
    "description": "更新议题",
    "createdAt": "2025-10-27T18:30:00",
    "updatedAt": "2025-10-27T20:15:00",
    "isDeleted": 0
  }
}
```

**失败响应**:
```json
{
  "success": false,
  "message": "日程ID不能为空",
  "data": null
}
```

---

### 7. 删除日程

删除指定的日程（软删除，数据不会真正删除）。

#### 请求信息

- **URL**: `/api/schedule/delete/{scheduleId}`
- **Method**: `DELETE`

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| scheduleId | String | ✅ | 日程ID | `"schedule-id-001"` |

#### 请求示例

```http
DELETE /api/schedule/delete/schedule-id-001
```

#### 响应示例

**成功响应**:
```json
{
  "success": true,
  "message": "删除成功",
  "data": null
}
```

**失败响应**:
```json
{
  "success": false,
  "message": "删除失败",
  "data": null
}
```

---

## 数据模型

### Schedule (日程对象)

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | String | 日程ID（UUID） | `"a1b2c3d4-e5f6-7890-abcd-ef1234567890"` |
| userId | String | 用户ID | `"user123"` |
| title | String | 日程标题 | `"团队会议"` |
| location | String | 地点 | `"会议室A"` |
| date | String | 日期 (yyyy-MM-dd) | `"2025-10-28"` |
| time | String | 时间 (HH:mm:ss) | `"14:30:00"` |
| description | String | 备注描述 | `"讨论Q4规划"` |
| createdAt | String | 创建时间 (ISO 8601) | `"2025-10-27T18:30:00"` |
| updatedAt | String | 更新时间 (ISO 8601) | `"2025-10-27T18:30:00"` |
| isDeleted | Integer | 删除标记 (0-未删除, 1-已删除) | `0` |

---

## 通用响应格式

所有接口的响应都遵循统一格式：

```typescript
{
  success: boolean,      // 操作是否成功
  message: string | null, // 提示信息（成功时可能为 null）
  data: object | array | null, // 返回的数据
  total?: number         // 列表接口会包含总数
}
```

---

## 错误码说明

| HTTP状态码 | 说明 |
|-----------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误（缺少必填参数、参数格式错误等） |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 前端集成示例

### 使用 axios

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/schedule';

// 创建日程
async function createSchedule(scheduleData) {
  try {
    const response = await axios.post(`${API_BASE_URL}/create`, scheduleData);
    if (response.data.success) {
      console.log('创建成功', response.data.data);
      return response.data.data;
    }
  } catch (error) {
    console.error('创建失败', error);
    throw error;
  }
}

// 获取某天的日程
async function getSchedulesByDate(userId, date) {
  try {
    const response = await axios.get(`${API_BASE_URL}/date`, {
      params: { userId, date }
    });
    return response.data.data;
  } catch (error) {
    console.error('获取日程失败', error);
    throw error;
  }
}

// 更新日程
async function updateSchedule(scheduleData) {
  try {
    const response = await axios.put(`${API_BASE_URL}/update`, scheduleData);
    return response.data;
  } catch (error) {
    console.error('更新失败', error);
    throw error;
  }
}

// 删除日程
async function deleteSchedule(scheduleId) {
  try {
    const response = await axios.delete(`${API_BASE_URL}/delete/${scheduleId}`);
    return response.data;
  } catch (error) {
    console.error('删除失败', error);
    throw error;
  }
}
```

### 使用 fetch

```javascript
const API_BASE_URL = 'http://localhost:8080/api/schedule';

// 创建日程
async function createSchedule(scheduleData) {
  const response = await fetch(`${API_BASE_URL}/create`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(scheduleData)
  });
  return await response.json();
}

// 获取日期范围内的日程
async function getSchedulesInRange(userId, startDate, endDate) {
  const params = new URLSearchParams({ userId, startDate, endDate });
  const response = await fetch(`${API_BASE_URL}/range?${params}`);
  return await response.json();
}
```

---

## 注意事项

1. **日期格式**: 所有日期必须使用 `yyyy-MM-dd` 格式（如 `2025-10-28`）
2. **时间格式**: 时间必须使用 `HH:mm:ss` 格式（如 `14:30:00`）
3. **软删除**: 删除操作不会真正删除数据，只是标记为已删除（`isDeleted = 1`）
4. **自动排序**: 所有查询接口返回的日程列表都会按 `date` 和 `time` 升序排列
5. **跨域**: 后端已配置 CORS，前端可直接调用
6. **字符编码**: 请确保使用 UTF-8 编码，支持中文标题和描述

---

## 常见使用场景

### 1. 日历视图（月视图）
```javascript
// 获取本月所有日程
const startDate = '2025-10-01';
const endDate = '2025-10-31';
const schedules = await getSchedulesInRange(userId, startDate, endDate);
```

### 2. 今日日程
```javascript
// 获取今天的日程
const today = new Date().toISOString().split('T')[0]; // 2025-10-27
const todaySchedules = await getSchedulesByDate(userId, today);
```

### 3. 周视图
```javascript
// 获取本周日程
const monday = '2025-10-27';
const sunday = '2025-11-02';
const weekSchedules = await getSchedulesInRange(userId, monday, sunday);
```

---

## 版本信息

- **文档版本**: v1.0
- **更新日期**: 2025-10-27
- **接口版本**: v1

如有问题，请联系后端开发团队。
