package com.gdmu;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedisConnection() {
        try {
            // 测试连接
            redisTemplate.opsForValue().set("test", "Hello Redis");
            String value = (String) redisTemplate.opsForValue().get("test");
            System.out.println("Redis连接测试成功，存储的值: " + value);
        } catch (Exception e) {
            System.err.println("Redis连接测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
