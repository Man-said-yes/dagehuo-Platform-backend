package com.gdmu.mapper;

import com.gdmu.pojo.Message;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息Mapper接口
 */
@Mapper
public interface MessageMapper {
    
    /**
     * 发送消息
     */
    @Insert("INSERT INTO messages(user_id, title, content, type, create_time) VALUES(#{userId}, #{title}, #{content}, #{type}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);
    
    /**
     * 根据ID查询消息
     */
    @Select("SELECT * FROM messages WHERE id = #{id}")
    Message selectById(Long id);
    
    /**
     * 根据用户ID查询消息列表
     */
    @Select("SELECT * FROM messages WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Message> selectByUserId(Long userId);
    
    /**
     * 查询未读消息数量
     */
    @Select("SELECT COUNT(*) FROM messages WHERE user_id = #{userId} AND is_read = 0")
    int countUnreadByUserId(Long userId);
    
    /**
     * 标记消息为已读
     */
    @Update("UPDATE messages SET is_read = 1 WHERE id = #{id}")
    int markAsRead(Long id);
    
    /**
     * 标记所有消息为已读
     */
    @Update("UPDATE messages SET is_read = 1 WHERE user_id = #{userId}")
    int markAllAsRead(Long userId);
}
