package com.gdmu.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdmu.pojo.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisActivityUtil {
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String ACTIVITY_LIST_PREFIX = "activity:list:";
    private static final String ACTIVITY_SEARCH_PREFIX = "activity:search:";
    private static final long CACHE_EXPIRE_TIME = 5;
    
    public String buildListKey(int page, int pageSize, String sortBy, String order) {
        return ACTIVITY_LIST_PREFIX + "all:" + page + ":" + pageSize + ":" + sortBy + ":" + order;
    }
    
    public String buildListByTypeKey(Integer type, int page, int pageSize, String sortBy, String order) {
        return ACTIVITY_LIST_PREFIX + "type:" + type + ":" + page + ":" + pageSize + ":" + sortBy + ":" + order;
    }
    
    public String buildListByDistanceKey(Double longitude, Double latitude, Integer type, int page, int pageSize, String order) {
        String typeStr = type != null ? type.toString() : "all";
        return ACTIVITY_LIST_PREFIX + "distance:" + longitude + ":" + latitude + ":" + typeStr + ":" + page + ":" + pageSize + ":" + order;
    }
    
    public String buildSearchKey(String keyword) {
        return ACTIVITY_SEARCH_PREFIX + keyword;
    }
    
    public void cacheActivityList(String key, List<Activity> activities) {
        try {
            String json = objectMapper.writeValueAsString(activities);
            stringRedisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
            log.info("缓存活动列表成功，key: {}", key);
        } catch (Exception e) {
            log.error("缓存活动列表失败，key: {}, error: {}", key, e.getMessage());
        }
    }
    
    public List<Activity> getActivityList(String key) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (json != null) {
                List<Activity> activities = objectMapper.readValue(json, new TypeReference<List<Activity>>() {});
                log.info("从缓存获取活动列表成功，key: {}", key);
                return activities;
            }
        } catch (Exception e) {
            log.error("从缓存获取活动列表失败，key: {}, error: {}", key, e.getMessage());
        }
        return null;
    }
    
    public void clearAllActivityCache() {
        try {
            Set<String> keys = stringRedisTemplate.keys(ACTIVITY_LIST_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
                log.info("清除活动列表缓存成功，共清除{}个key", keys.size());
            }
            
            Set<String> searchKeys = stringRedisTemplate.keys(ACTIVITY_SEARCH_PREFIX + "*");
            if (searchKeys != null && !searchKeys.isEmpty()) {
                stringRedisTemplate.delete(searchKeys);
                log.info("清除活动搜索缓存成功，共清除{}个key", searchKeys.size());
            }
        } catch (Exception e) {
            log.error("清除活动缓存失败: {}", e.getMessage());
        }
    }
}
