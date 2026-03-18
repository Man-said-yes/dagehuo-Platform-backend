# WebSocket消息接收问题排查

## 问题描述

同一群组的人，有些人收不到消息

## 问题原因分析

### 核心问题：在线用户列表不一致

WebSocket消息广播依赖Redis中的在线用户列表，如果用户不在在线列表中，就收不到消息。

### 可能的场景

#### 场景1：用户连接顺序问题

```
时间线：
1. 用户A连接WebSocket ✅
2. 用户A加入群聊 ✅
3. 用户A的WebSocket连接建立，获取群列表（此时A已在群中）✅
4. 用户A被添加到群的在线列表 ✅
5. 用户A能收到消息 ✅
```

```
时间线（问题场景）：
1. 用户B连接WebSocket ✅
2. WebSocket连接建立，获取群列表（此时B不在任何群中）❌
3. 用户B没有被添加到任何群的在线列表 ❌
4. 用户B加入群聊 ✅
5. 但是B的WebSocket连接已经建立，不会重新获取群列表 ❌
6. 用户B不在群的在线列表中，收不到消息 ❌
```

#### 场景2：Redis在线列表未更新

```
用户A在群1中：
- WebSocket连接时被添加到群1的在线列表 ✅
- 能收到群1的消息 ✅

用户A加入群2：
- 数据库添加群成员关系 ✅
- 但是WebSocket连接已经建立，不会自动添加到群2的在线列表 ❌
- 收不到群2的消息 ❌
```

---

## 解决方案

### 方案1：修改addGroupMember方法（已实现）

代码中已经处理了这种情况：

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void addGroupMember(Long groupId, Long userId) {
    log.info("添加群成员，groupId: {}, userId: {}", groupId, userId);
    
    ChatGroupMember existingMember = chatGroupMemberMapper.selectByGroupIdAndUserId(groupId, userId);
    if (existingMember == null) {
        ChatGroupMember member = new ChatGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        
        int rows = chatGroupMemberMapper.insert(member);
        if (rows <= 0) {
            throw new RuntimeException("添加群成员失败");
        }
        
        // 如果用户已经在线，立即添加到该群的在线用户列表
        com.gdmu.websocket.ChatWebSocketHandler handler = getChatWebSocketHandler();
        if (handler.isUserOnline(userId)) {
            log.info("用户{}已在线，添加到群{}的在线列表", userId, groupId);
            redisChatUtil.addOnlineUser(groupId, userId);
            // 广播用户加入群的消息
            handler.broadcastUserStatus(groupId, userId, true);
        }
    }
}
```

### 方案2：前端主动刷新在线状态（推荐）

当用户加入新群后，前端可以主动调用一个接口刷新在线状态。

#### 新增接口：刷新用户在线状态

在 `ChatController.java` 中添加：

```java
@Operation(summary = "刷新用户在线状态", description = "将用户添加到所有所在群的在线列表")
@PostMapping("/refresh-online-status")
public Result refreshOnlineStatus() {
    try {
        Long userId = getCurrentUserId();
        chatService.refreshUserOnlineStatus(userId);
        return Result.success("刷新成功");
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

在 `ChatService.java` 中添加：

```java
/**
 * 刷新用户在线状态
 */
void refreshUserOnlineStatus(Long userId);
```

在 `ChatServiceImpl.java` 中实现：

```java
@Override
public void refreshUserOnlineStatus(Long userId) {
    log.info("刷新用户在线状态，userId: {}", userId);
    
    com.gdmu.websocket.ChatWebSocketHandler handler = getChatWebSocketHandler();
    if (handler.isUserOnline(userId)) {
        // 获取用户所在的所有群
        List<ChatGroup> groups = chatGroupMapper.selectByUserId(userId);
        if (groups != null) {
            for (ChatGroup group : groups) {
                // 添加用户到群的在线列表
                redisChatUtil.addOnlineUser(group.getId(), userId);
                log.info("用户{}已添加到群{}的在线列表", userId, group.getId());
            }
        }
    }
}
```

### 方案3：前端重新连接WebSocket（临时方案）

当用户加入新群后，前端可以断开并重新连接WebSocket：

```javascript
// 加入群聊后重新连接WebSocket
async joinGroup(groupId) {
  await this.$api.chat.joinGroup(groupId)
  
  // 重新连接WebSocket
  websocketService.close()
  setTimeout(() => {
    const token = uni.getStorageSync('token')
    websocketService.connect(token)
  }, 1000)
}
```

---

## 排查步骤

### 1. 检查Redis在线用户列表

```bash
# 连接Redis
redis-cli

# 查看群1的在线用户
SMEMBERS chat:group:1:online

# 查看所有群的在线用户
KEYS chat:group:*:online
```

### 2. 检查用户是否在群聊中

```sql
-- 查看用户所在的群
SELECT cg.*, cgm.user_id 
FROM chat_group cg
LEFT JOIN chat_group_member cgm ON cg.id = cgm.group_id
WHERE cgm.user_id = 你的用户ID;

-- 查看群聊的所有成员
SELECT * FROM chat_group_member WHERE group_id = 你的群ID;
```

### 3. 查看服务器日志

关键日志：
```
用户1所在的群列表: 2
用户1已添加到群1的在线列表
用户1已添加到群2的在线列表
从Redis获取的群1在线用户列表: [1, 2, 3]
消息广播完成，共发送给3个用户
```

如果看到：
```
用户1所在的群列表: 0  // 问题：用户没有加入任何群
消息广播完成，共发送给0个用户  // 问题：群内没有在线用户
```

---

## 测试步骤

### 1. 使用测试页面测试

打开 `test-websocket.html` 测试页面：

1. 用户A登录并连接WebSocket
2. 用户B登录并连接WebSocket
3. 检查Redis中群1的在线用户列表
4. 用户A发送消息，查看用户B是否收到

### 2. 测试新用户加入群聊

1. 用户C登录并连接WebSocket
2. 用户C加入群聊
3. 检查Redis中群1的在线用户列表是否包含用户C
4. 用户A发送消息，查看用户C是否收到

---

## 临时解决方案

如果问题紧急，可以：

### 1. 重启服务器

重启后所有用户重新连接WebSocket，在线列表会重新初始化。

### 2. 手动更新Redis

```bash
# 手动添加用户到群的在线列表
SADD chat:group:1:online 用户ID
```

### 3. 让用户重新登录

用户重新登录后，WebSocket会重新连接，在线列表会重新初始化。

---

## 长期解决方案

建议实现方案2（刷新在线状态接口），这样：

1. 用户加入新群后，前端调用刷新接口
2. 后端将用户添加到所有所在群的在线列表
3. 用户能立即收到新群的消息

这个方案最稳定，不会影响现有功能。
