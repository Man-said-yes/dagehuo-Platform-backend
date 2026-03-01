package com.gdmu.mapper;

import com.gdmu.pojo.ChatGroup;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatGroupMapper {
    @Insert("INSERT INTO chat_group(name, activity_id) VALUES(#{name}, #{activityId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatGroup chatGroup);

    @Select("SELECT * FROM chat_group WHERE id = #{id}")
    ChatGroup selectById(Long id);

    @Select("SELECT * FROM chat_group WHERE activity_id = #{activityId}")
    ChatGroup selectByActivityId(Long activityId);

    @Select("SELECT cg.* FROM chat_group cg JOIN chat_group_member cgm ON cg.id = cgm.group_id WHERE cgm.user_id = #{userId} ORDER BY cg.create_time DESC")
    List<ChatGroup> selectByUserId(Long userId);

    @Update("UPDATE chat_group SET name = #{name}, activity_id = #{activityId} WHERE id = #{id}")
    int update(ChatGroup chatGroup);

    @Delete("DELETE FROM chat_group WHERE id = #{id}")
    int deleteById(Long id);
}
