# WebSocket 问题排查指南

## 一、问题描述

前端反馈：WebSocket能连上，但一直用不了

## 二、可能的原因

### 1. 用户没有加入任何群聊

**问题说明**：
- WebSocket连接成功后，会自动获取用户所在的群
- 如果用户没有加入任何群，就无法发送消息

**排查方法**：
```sql
-- 查看用户是否加入了群聊
SELECT * FROM chat_group_member WHERE user_id = 你的用户ID;

-- 查看群聊列表
SELECT * FROM chat_group WHERE status = 1;
```

**解决方案**：
- 用户需要先加入一个活动，才能有群聊
- 或者手动添加用户到群聊：
```sql
INSERT INTO chat_group_member (group_id, user_id, join_time) VALUES (1, 你的用户ID, NOW());
```

---

### 2. 消息格式错误

**正确的消息格式**：
```json
{
  "groupId": 1,
  "content": "大家好"
}
```

**常见错误格式**：
```json
// ❌ 错误：多了type字段
{
  "type": "chat",
  "groupId": 1,
  "content": "大家好"
}

// ❌ 错误：字段名不对
{
  "group_id": 1,
  "content": "大家好"
}

// ❌ 错误：groupId是字符串
{
  "groupId": "1",
  "content": "大家好"
}
```

---

### 3. Redis连接问题

**排查方法**：
- 查看服务器日志，是否有Redis连接错误
- 检查Redis是否正常运行

**解决方案**：
```bash
# 检查Redis状态
redis-cli ping

# 如果Redis未运行，启动Redis
redis-server
```

---

### 4. WebSocket连接地址错误

**正确的连接地址**：
```
ws://81.71.119.69:8080/ws/chat?token=YOUR_TOKEN
```

**常见错误**：
- ❌ 使用了http://而不是ws://
- ❌ token参数名错误（不是jwt或authorization）
- ❌ token过期或无效

---

## 三、排查步骤

### 步骤1：检查WebSocket连接日志

**服务器端日志**：
```
WebSocket认证，token: eyJhbGciOiJIUzI1NiIs...
WebSocket认证成功，userId: 1
WebSocket连接建立，userId: 1
用户会话已存储，当前在线用户数: 1
用户1所在的群列表: 0
```

**如果看到"用户所在的群列表: 0"**：
- 说明用户没有加入任何群聊
- 需要先让用户加入一个群

---

### 步骤2：检查消息发送日志

**服务器端日志**：
```
收到WebSocket消息，userId: 1, content: {"groupId":1,"content":"大家好"}
解析消息成功，groupId: 1, content: 大家好
消息已保存到数据库，messageId: 1
准备广播消息: {"type":"chat_message","messageId":1,...}
消息广播完成，共发送给1个用户
```

**如果没有看到"收到WebSocket消息"**：
- 说明前端没有发送消息
- 或者消息格式错误导致解析失败

**如果看到"共发送给0个用户"**：
- 说明群内没有在线用户
- 检查Redis是否正常工作

---

### 步骤3：前端调试

**在浏览器控制台执行**：
```javascript
// 1. 检查WebSocket连接状态
console.log('WebSocket状态:', websocketService.isConnected)

// 2. 发送测试消息
websocketService.sendChatMessage(1, '测试消息')

// 3. 查看消息队列
console.log('消息队列:', websocketService.messageQueue)
```

---

## 四、测试WebSocket的HTML页面

创建一个测试页面 `test-websocket.html`：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WebSocket测试</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 50px auto;
            padding: 20px;
        }
        .section {
            margin-bottom: 30px;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 8px;
        }
        h2 {
            margin-top: 0;
        }
        input, button {
            padding: 10px;
            margin: 5px;
            border-radius: 4px;
            border: 1px solid #ddd;
        }
        button {
            background: #07c160;
            color: white;
            border: none;
            cursor: pointer;
        }
        button:hover {
            background: #06ad56;
        }
        .status {
            padding: 10px;
            margin: 10px 0;
            border-radius: 4px;
        }
        .connected {
            background: #d4edda;
            color: #155724;
        }
        .disconnected {
            background: #f8d7da;
            color: #721c24;
        }
        #messages {
            height: 300px;
            overflow-y: scroll;
            border: 1px solid #ddd;
            padding: 10px;
            background: #f9f9f9;
        }
        .message {
            margin: 5px 0;
            padding: 5px;
            border-bottom: 1px solid #eee;
        }
    </style>
</head>
<body>
    <h1>WebSocket测试页面</h1>
    
    <!-- 连接状态 -->
    <div class="section">
        <h2>连接状态</h2>
        <div id="status" class="status disconnected">未连接</div>
    </div>
    
    <!-- 登录 -->
    <div class="section">
        <h2>登录</h2>
        <input type="text" id="username" placeholder="用户名" value="test001">
        <input type="password" id="password" placeholder="密码" value="123456">
        <button onclick="login()">登录</button>
        <div id="token" style="margin-top:10px; word-break: break-all;"></div>
    </div>
    
    <!-- WebSocket连接 -->
    <div class="section">
        <h2>WebSocket连接</h2>
        <button onclick="connectWebSocket()">连接WebSocket</button>
        <button onclick="disconnectWebSocket()">断开连接</button>
    </div>
    
    <!-- 发送消息 -->
    <div class="section">
        <h2>发送消息</h2>
        <input type="number" id="groupId" placeholder="群聊ID" value="1">
        <input type="text" id="messageContent" placeholder="消息内容" value="大家好">
        <button onclick="sendMessage()">发送消息</button>
    </div>
    
    <!-- 消息列表 -->
    <div class="section">
        <h2>消息列表</h2>
        <div id="messages"></div>
    </div>

    <script>
        let token = '';
        let userId = '';
        let websocket = null;
        
        // 登录
        async function login() {
            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            
            try {
                const response = await fetch('http://81.71.119.69:8080/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ username, password })
                });
                
                const data = await response.json();
                console.log('登录响应:', data);
                
                if (data.code === 200) {
                    token = data.data.TOKEN;
                    userId = data.data.userId;
                    document.getElementById('token').innerHTML = 
                        `<strong>Token:</strong> ${token.substring(0, 50)}...<br>
                         <strong>UserId:</strong> ${userId}`;
                    alert('登录成功！');
                } else {
                    alert('登录失败：' + data.msg);
                }
            } catch (error) {
                console.error('登录失败:', error);
                alert('登录失败：' + error.message);
            }
        }
        
        // 连接WebSocket
        function connectWebSocket() {
            if (!token) {
                alert('请先登录！');
                return;
            }
            
            const wsUrl = `ws://81.71.119.69:8080/ws/chat?token=${token}`;
            console.log('连接WebSocket:', wsUrl);
            
            websocket = new WebSocket(wsUrl);
            
            websocket.onopen = function(event) {
                console.log('WebSocket连接成功');
                updateStatus(true);
                addMessage('系统', 'WebSocket连接成功');
            };
            
            websocket.onmessage = function(event) {
                console.log('收到消息:', event.data);
                const message = JSON.parse(event.data);
                
                if (message.type === 'chat_message') {
                    addMessage(`用户${message.userId}`, message.content);
                } else if (message.type === 'user_status') {
                    const status = message.isOnline ? '上线了' : '下线了';
                    addMessage('系统', `用户${message.userId} ${status}`);
                }
            };
            
            websocket.onclose = function(event) {
                console.log('WebSocket连接关闭');
                updateStatus(false);
                addMessage('系统', 'WebSocket连接关闭');
            };
            
            websocket.onerror = function(error) {
                console.error('WebSocket错误:', error);
                updateStatus(false);
                addMessage('系统', 'WebSocket连接错误');
            };
        }
        
        // 断开WebSocket
        function disconnectWebSocket() {
            if (websocket) {
                websocket.close();
                websocket = null;
            }
        }
        
        // 发送消息
        function sendMessage() {
            if (!websocket || websocket.readyState !== WebSocket.OPEN) {
                alert('WebSocket未连接！');
                return;
            }
            
            const groupId = parseInt(document.getElementById('groupId').value);
            const content = document.getElementById('messageContent').value;
            
            const message = {
                groupId: groupId,
                content: content
            };
            
            console.log('发送消息:', message);
            websocket.send(JSON.stringify(message));
            addMessage('我', content);
        }
        
        // 更新连接状态
        function updateStatus(connected) {
            const statusDiv = document.getElementById('status');
            if (connected) {
                statusDiv.className = 'status connected';
                statusDiv.textContent = '已连接';
            } else {
                statusDiv.className = 'status disconnected';
                statusDiv.textContent = '未连接';
            }
        }
        
        // 添加消息到列表
        function addMessage(sender, content) {
            const messagesDiv = document.getElementById('messages');
            const messageDiv = document.createElement('div');
            messageDiv.className = 'message';
            messageDiv.innerHTML = `<strong>${sender}:</strong> ${content}`;
            messagesDiv.appendChild(messageDiv);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        }
    </script>
</body>
</html>
```

---

## 五、常见问题解决方案

### 问题1：用户没有加入群聊

**解决方案**：
```sql
-- 1. 创建一个测试群
INSERT INTO chat_group (name, type, status, owner_id) 
VALUES ('测试群', 2, 1, 1);

-- 2. 将用户加入群聊
INSERT INTO chat_group_member (group_id, user_id, join_time) 
VALUES (1, 你的用户ID, NOW());
```

### 问题2：Redis未运行

**解决方案**：
```bash
# 启动Redis
redis-server

# 或者使用Docker
docker run -d -p 6379:6379 redis
```

### 问题3：Token过期

**解决方案**：
- 重新登录获取新token
- Token有效期24小时

---

## 六、联系后端开发者

如果以上排查都无法解决问题，请提供以下信息：

1. **服务器日志**：WebSocket连接和消息发送的完整日志
2. **前端代码**：WebSocket连接和消息发送的代码
3. **用户ID**：测试用户的ID
4. **群聊ID**：测试群聊的ID

**后端开发者会检查**：
- 用户是否在群聊中
- Redis是否正常工作
- 消息是否正确保存到数据库
