# uniapp WebSocket 使用指南（项目专用版）

## 一、WebSocket 连接说明

### 连接地址
```
ws://你的服务器IP:8080/ws/chat?token=YOUR_JWT_TOKEN
```

### 连接要求
- 必须在连接时携带有效的JWT token
- token通过登录接口获取：`POST /api/auth/login`
- token放在URL参数中

### 连接后自动处理
1. **用户上线**：自动将用户添加到所在群的在线列表
2. **广播上线通知**：向群内其他用户发送上线通知
3. **会话存储**：服务器存储用户会话，用于消息推送

---

## 二、消息格式说明（重要！）

### 1. 发送聊天消息

**前端发送格式**：
```json
{
  "groupId": 1,
  "content": "大家好"
}
```

**字段说明**：
- `groupId`：群聊ID（必填）
- `content`：消息内容（必填）

### 2. 接收聊天消息

**后端推送格式**：
```json
{
  "type": "chat_message",
  "messageId": 1,
  "groupId": 1,
  "userId": 1,
  "content": "大家好",
  "sendTime": "2024-01-01T10:00:00.000Z"
}
```

**字段说明**：
- `type`：消息类型，固定为 `chat_message`
- `messageId`：消息ID
- `groupId`：群聊ID
- `userId`：发送者ID
- `content`：消息内容
- `sendTime`：发送时间（ISO 8601格式）

### 3. 接收用户状态消息

**后端推送格式**：
```json
{
  "type": "user_status",
  "groupId": 1,
  "userId": 1,
  "isOnline": true
}
```

**字段说明**：
- `type`：消息类型，固定为 `user_status`
- `groupId`：群聊ID
- `userId`：用户ID
- `isOnline`：是否在线（true-上线，false-下线）

---

## 三、uniapp 完整实现代码

### 1. 创建 WebSocket 工具类

在 `utils/websocket.js` 中创建：

```javascript
class WebSocketService {
  constructor() {
    this.socketTask = null
    this.isConnected = false
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.messageQueue = []
    this.listeners = {}
  }

  /**
   * 连接WebSocket
   * @param {String} token - JWT token
   */
  connect(token) {
    if (this.isConnected) {
      console.log('WebSocket已连接')
      return
    }

    // 替换为你的服务器地址
    const url = `ws://你的服务器IP:8080/ws/chat?token=${token}`
    
    this.socketTask = uni.connectSocket({
      url: url,
      success: () => {
        console.log('WebSocket连接中...')
      },
      fail: (err) => {
        console.error('WebSocket连接失败:', err)
        this.reconnect(token)
      }
    })

    this.initEventListeners()
  }

  /**
   * 初始化事件监听
   */
  initEventListeners() {
    // 连接打开
    this.socketTask.onOpen(() => {
      console.log('WebSocket连接成功')
      this.isConnected = true
      this.startHeartbeat()
      
      // 发送队列中的消息
      this.flushMessageQueue()
      
      // 触发连接成功回调
      this.emit('open')
    })

    // 接收消息
    this.socketTask.onMessage((res) => {
      try {
        const message = JSON.parse(res.data)
        console.log('收到消息:', message)
        
        // 根据消息类型分发
        if (message.type === 'chat_message') {
          this.emit('chat', message)
        } else if (message.type === 'user_status') {
          this.emit('status', message)
        }
      } catch (e) {
        console.error('消息解析失败:', e)
      }
    })

    // 连接关闭
    this.socketTask.onClose(() => {
      console.log('WebSocket连接关闭')
      this.isConnected = false
      this.stopHeartbeat()
      this.emit('close')
    })

    // 连接错误
    this.socketTask.onError((err) => {
      console.error('WebSocket错误:', err)
      this.isConnected = false
      this.emit('error', err)
    })
  }

  /**
   * 发送聊天消息
   * @param {Number} groupId - 群聊ID
   * @param {String} content - 消息内容
   */
  sendChatMessage(groupId, content) {
    const message = {
      groupId: groupId,
      content: content
    }
    this.send(message)
  }

  /**
   * 发送消息（底层方法）
   * @param {Object} message - 消息对象
   */
  send(message) {
    if (!this.isConnected) {
      console.log('WebSocket未连接，消息加入队列')
      this.messageQueue.push(message)
      return
    }

    this.socketTask.send({
      data: JSON.stringify(message),
      success: () => {
        console.log('消息发送成功')
      },
      fail: (err) => {
        console.error('消息发送失败:', err)
      }
    })
  }

  /**
   * 发送队列中的消息
   */
  flushMessageQueue() {
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift()
      this.send(message)
    }
  }

  /**
   * 开始心跳（可选，服务器未要求心跳）
   */
  startHeartbeat() {
    // 如果服务器不需要心跳，可以注释掉
    // this.heartbeatTimer = setInterval(() => {
    //   if (this.isConnected) {
    //     this.send({ type: 'heartbeat' })
    //   }
    // }, 30000)
  }

  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 重连
   * @param {String} token - JWT token
   */
  reconnect(token) {
    if (this.reconnectTimer) return

    this.reconnectTimer = setTimeout(() => {
      console.log('尝试重新连接WebSocket...')
      this.connect(token)
      this.reconnectTimer = null
    }, 5000)
  }

  /**
   * 关闭连接
   */
  close() {
    if (this.socketTask) {
      this.stopHeartbeat()
      this.socketTask.close({
        success: () => {
          console.log('WebSocket已关闭')
          this.isConnected = false
        }
      })
    }
  }

  /**
   * 添加事件监听器
   * @param {String} event - 事件名称（open, close, error, chat, status）
   * @param {Function} callback - 回调函数
   */
  on(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  }

  /**
   * 移除事件监听器
   */
  off(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => callback(data))
    }
  }
}

// 创建单例
const websocketService = new WebSocketService()

export default websocketService
```

---

### 2. 在聊天室页面使用

在 `pages/chat/room.vue` 中：

```vue
<template>
  <view class="chat-room">
    <!-- 消息列表 -->
    <scroll-view 
      class="message-list" 
      scroll-y 
      :scroll-top="scrollTop"
    >
      <view 
        class="message-item" 
        v-for="(msg, index) in messageList" 
        :key="index"
      >
        <!-- 用户状态消息 -->
        <view v-if="msg.type === 'user_status'" class="status-message">
          {{ msg.userId }} {{ msg.isOnline ? '上线了' : '下线了' }}
        </view>
        
        <!-- 聊天消息 -->
        <view v-else class="chat-message" :class="{ 'my-message': msg.userId === currentUserId }">
          <view class="user-info">
            <text class="nickname">用户{{ msg.userId }}</text>
          </view>
          <view class="message-content">{{ msg.content }}</view>
          <view class="message-time">{{ formatTime(msg.sendTime) }}</view>
        </view>
      </view>
    </scroll-view>

    <!-- 输入框 -->
    <view class="input-area">
      <input 
        v-model="inputMessage" 
        placeholder="输入消息..." 
        @confirm="sendMessage"
      />
      <button @click="sendMessage">发送</button>
    </view>
  </view>
</template>

<script>
import websocketService from '@/utils/websocket.js'

export default {
  data() {
    return {
      groupId: null,
      currentUserId: null,
      inputMessage: '',
      messageList: [],
      scrollTop: 0
    }
  },
  
  onLoad(options) {
    this.groupId = parseInt(options.groupId)
    this.currentUserId = parseInt(uni.getStorageSync('userId'))
    
    // 连接WebSocket
    this.connectWebSocket()
    
    // 加载历史消息
    this.loadHistoryMessages()
  },
  
  onUnload() {
    // 页面卸载时移除监听
    websocketService.off('chat', this.handleChatMessage)
    websocketService.off('status', this.handleStatusMessage)
  },
  
  methods: {
    /**
     * 连接WebSocket
     */
    connectWebSocket() {
      const token = uni.getStorageSync('token')
      
      // 连接WebSocket
      websocketService.connect(token)
      
      // 监听聊天消息
      websocketService.on('chat', this.handleChatMessage)
      
      // 监听用户状态消息
      websocketService.on('status', this.handleStatusMessage)
      
      // 监听连接成功
      websocketService.on('open', () => {
        uni.showToast({
          title: '连接成功',
          icon: 'success'
        })
      })
      
      // 监听连接错误
      websocketService.on('error', () => {
        uni.showToast({
          title: '连接失败',
          icon: 'none'
        })
      })
    },
    
    /**
     * 处理聊天消息
     */
    handleChatMessage(message) {
      console.log('收到聊天消息:', message)
      
      // 添加到消息列表
      this.messageList.push({
        type: 'chat_message',
        messageId: message.messageId,
        groupId: message.groupId,
        userId: message.userId,
        content: message.content,
        sendTime: message.sendTime
      })
      
      // 滚动到底部
      this.scrollToBottom()
    },
    
    /**
     * 处理用户状态消息
     */
    handleStatusMessage(message) {
      console.log('收到状态消息:', message)
      
      // 添加到消息列表
      this.messageList.push({
        type: 'user_status',
        groupId: message.groupId,
        userId: message.userId,
        isOnline: message.isOnline
      })
      
      // 滚动到底部
      this.scrollToBottom()
    },
    
    /**
     * 发送消息
     */
    sendMessage() {
      if (!this.inputMessage.trim()) {
        return
      }
      
      // 发送消息
      websocketService.sendChatMessage(this.groupId, this.inputMessage.trim())
      
      // 清空输入框
      this.inputMessage = ''
    },
    
    /**
     * 加载历史消息
     */
    async loadHistoryMessages() {
      try {
        const res = await this.$api.chat.getMessages(this.groupId, 1, 20)
        this.messageList = res.map(msg => ({
          type: 'chat_message',
          messageId: msg.id,
          groupId: msg.groupId,
          userId: msg.userId,
          content: msg.content,
          sendTime: msg.sendTime
        })).reverse()
        
        this.scrollToBottom()
      } catch (e) {
        console.error('加载历史消息失败:', e)
      }
    },
    
    /**
     * 滚动到底部
     */
    scrollToBottom() {
      this.$nextTick(() => {
        this.scrollTop = 999999
      })
    },
    
    /**
     * 格式化时间
     */
    formatTime(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      const hours = date.getHours().toString().padStart(2, '0')
      const minutes = date.getMinutes().toString().padStart(2, '0')
      return `${hours}:${minutes}`
    }
  }
}
</script>

<style scoped>
.chat-room {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.message-list {
  flex: 1;
  padding: 20rpx;
}

.message-item {
  margin-bottom: 20rpx;
}

.status-message {
  text-align: center;
  color: #999;
  font-size: 24rpx;
  padding: 10rpx 0;
}

.chat-message {
  max-width: 70%;
  padding: 20rpx;
  background: #fff;
  border-radius: 10rpx;
}

.my-message {
  margin-left: auto;
  background: #95ec69;
}

.user-info {
  margin-bottom: 10rpx;
}

.nickname {
  font-size: 24rpx;
  color: #666;
}

.message-content {
  font-size: 28rpx;
  word-break: break-all;
}

.message-time {
  font-size: 20rpx;
  color: #999;
  margin-top: 10rpx;
  text-align: right;
}

.input-area {
  display: flex;
  align-items: center;
  padding: 20rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
}

.input-area input {
  flex: 1;
  height: 70rpx;
  padding: 0 20rpx;
  background: #f5f5f5;
  border-radius: 35rpx;
}

.input-area button {
  margin-left: 20rpx;
  padding: 0 30rpx;
  height: 70rpx;
  line-height: 70rpx;
  background: #07c160;
  color: #fff;
  border-radius: 35rpx;
  border: none;
}
</style>
```

---

### 3. 在 App.vue 中全局初始化

```vue
<script>
import websocketService from '@/utils/websocket.js'

export default {
  onLaunch() {
    // 检查登录状态
    const token = uni.getStorageSync('token')
    if (token) {
      // 自动连接WebSocket
      websocketService.connect(token)
    }
  },
  
  onShow() {
    // 小程序从后台进入前台时重连
    const token = uni.getStorageSync('token')
    if (token && !websocketService.isConnected) {
      websocketService.connect(token)
    }
  }
}
</script>
```

---

## 四、完整流程示例

### 1. 用户登录后连接WebSocket

```javascript
// 登录成功后
const res = await this.$api.auth.login({
  username: 'user001',
  password: '123456'
})

// 保存token和userId
uni.setStorageSync('token', res.TOKEN)
uni.setStorageSync('userId', res.userId)

// 连接WebSocket
import websocketService from '@/utils/websocket.js'
websocketService.connect(res.TOKEN)
```

### 2. 进入聊天室发送消息

```javascript
// 发送消息
websocketService.sendChatMessage(groupId, '大家好')

// 接收消息
websocketService.on('chat', (message) => {
  console.log('收到消息:', message.content)
})
```

### 3. 监听用户上下线

```javascript
websocketService.on('status', (message) => {
  if (message.isOnline) {
    console.log(`用户${message.userId}上线了`)
  } else {
    console.log(`用户${message.userId}下线了`)
  }
})
```

---

## 五、常见问题

### 1. 连接失败

**检查项**：
- token是否有效
- 服务器地址是否正确
- 网络是否正常

**解决方案**：
```javascript
// 检查token
const token = uni.getStorageSync('token')
if (!token) {
  uni.navigateTo({ url: '/pages/login/login' })
  return
}
```

### 2. 消息发送失败

**原因**：WebSocket未连接

**解决方案**：
```javascript
// 检查连接状态
if (!websocketService.isConnected) {
  uni.showToast({
    title: '连接中，请稍后...',
    icon: 'none'
  })
  return
}
```

### 3. 收不到消息

**检查项**：
- 是否正确监听了消息事件
- 是否在页面卸载时移除了监听

**解决方案**：
```javascript
// 确保监听
websocketService.on('chat', this.handleChatMessage)

// 页面卸载时移除监听
onUnload() {
  websocketService.off('chat', this.handleChatMessage)
}
```

---

## 六、调试技巧

### 1. 查看连接状态

```javascript
console.log('WebSocket状态:', websocketService.isConnected)
```

### 2. 查看消息队列

```javascript
console.log('消息队列:', websocketService.messageQueue)
```

### 3. 测试发送消息

```javascript
// 在控制台测试
websocketService.sendChatMessage(1, '测试消息')
```

---

## 七、项目实际消息流程

```
用户A发送消息
    ↓
前端调用：websocketService.sendChatMessage(groupId, content)
    ↓
发送JSON：{"groupId": 1, "content": "大家好"}
    ↓
后端接收并处理：
    1. 保存消息到数据库
    2. 构建广播消息
    3. 推送给群内所有在线用户
    ↓
前端接收消息：
{
  "type": "chat_message",
  "messageId": 1,
  "groupId": 1,
  "userId": 1,
  "content": "大家好",
  "sendTime": "2024-01-01T10:00:00.000Z"
}
```

---

**如有疑问，请及时联系后端开发者！** 📱
