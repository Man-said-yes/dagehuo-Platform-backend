package com.gdmu.mapper;

import com.gdmu.pojo.Participant;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 参与者Mapper接口
 */
@Mapper
public interface ParticipantMapper {
    
    /**
     * 报名参加搭伙活动
     */
    @Insert("INSERT INTO activity_participant(activity_id, user_id, status) VALUES(#{mealEventId}, #{userId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Participant participant);
    
    /**
     * 根据活动ID和用户ID查询参与者
     */
    @Select("SELECT * FROM activity_participant WHERE activity_id = #{mealEventId} AND user_id = #{userId}")
    Participant selectByMealEventIdAndUserId(Long mealEventId, Long userId);
    
    /**
     * 根据活动ID查询所有参与者
     */
    @Select("SELECT * FROM activity_participant WHERE activity_id = #{mealEventId}")
    List<Participant> selectByMealEventId(Long mealEventId);
    
    /**
     * 根据用户ID查询所有参与的活动
     */
    @Select("SELECT * FROM activity_participant WHERE user_id = #{userId}")
    List<Participant> selectByUserId(Long userId);
    
    /**
     * 更新参与者状态
     */
    @Update("UPDATE activity_participant SET status = #{status} WHERE id = #{id}")
    int updateStatus(Participant participant);
    
    /**
     * 取消参加活动
     */
    @Delete("DELETE FROM activity_participant WHERE activity_id = #{mealEventId} AND user_id = #{userId}")
    int deleteByMealEventIdAndUserId(Long mealEventId, Long userId);
}
