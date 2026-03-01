package com.gdmu.mapper;

import com.gdmu.pojo.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    @Insert("INSERT INTO chat_message(group_id, user_id, content, type) VALUES(#{groupId}, #{userId}, #{content}, #{type})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage chatMessage);

    @Select("SELECT * FROM chat_message WHERE id = #{id}")
    ChatMessage selectById(Long id);

    @Select("SELECT * FROM chat_message WHERE group_id = #{groupId} ORDER BY send_time DESC LIMIT #{offset}, #{limit}")
    List<ChatMessage> selectByGroupId(@Param("groupId") Long groupId, @Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM chat_message WHERE group_id = #{groupId}")
    int countByGroupId(Long groupId);

    @Select("SELECT * FROM chat_message WHERE group_id = #{groupId} ORDER BY send_time DESC LIMIT 1")
    ChatMessage selectLastMessageByGroupId(Long groupId);
}
