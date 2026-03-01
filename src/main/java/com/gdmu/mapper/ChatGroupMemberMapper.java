package com.gdmu.mapper;

import com.gdmu.pojo.ChatGroupMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatGroupMemberMapper {
    @Insert("INSERT INTO chat_group_member(group_id, user_id) VALUES(#{groupId}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatGroupMember chatGroupMember);

    @Select("SELECT * FROM chat_group_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    ChatGroupMember selectByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Select("SELECT * FROM chat_group_member WHERE group_id = #{groupId}")
    List<ChatGroupMember> selectByGroupId(Long groupId);

    @Select("SELECT * FROM chat_group_member WHERE user_id = #{userId}")
    List<ChatGroupMember> selectByUserId(Long userId);

    @Update("UPDATE chat_group_member SET last_read_message_id = #{messageId} WHERE group_id = #{groupId} AND user_id = #{userId}")
    int updateLastReadMessageId(@Param("groupId") Long groupId, @Param("userId") Long userId, @Param("messageId") Long messageId);

    @Delete("DELETE FROM chat_group_member WHERE group_id = #{groupId} AND user_id = #{userId}")
    int deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);
}
