package com.gdmu.mapper;

import com.gdmu.pojo.MealEvent;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 搭伙活动Mapper接口
 */
@Mapper
public interface MealEventMapper {
    
    /**
     * 创建搭伙活动
     */
    @Insert("INSERT INTO meal_events(title, description, event_time, location, max_people, creator_id) VALUES(#{title}, #{description}, #{eventTime}, #{location}, #{maxPeople}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MealEvent mealEvent);
    
    /**
     * 根据ID查询搭伙活动
     */
    @Select("SELECT * FROM meal_events WHERE id = #{id}")
    MealEvent selectById(Long id);
    
    /**
     * 查询所有搭伙活动（分页）
     */
    @Select("SELECT * FROM meal_events WHERE status = #{status} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<MealEvent> selectAll(Integer status, Integer offset, Integer limit);
    
    /**
     * 根据创建者ID查询搭伙活动
     */
    @Select("SELECT * FROM meal_events WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<MealEvent> selectByCreatorId(Long creatorId);
    
    /**
     * 更新搭伙活动信息
     */
    @Update("UPDATE meal_events SET title = #{title}, description = #{description}, event_time = #{eventTime}, location = #{location}, max_people = #{maxPeople}, status = #{status} WHERE id = #{id}")
    int update(MealEvent mealEvent);
    
    /**
     * 更新当前参与人数
     */
    @Update("UPDATE meal_events SET current_people = current_people + #{delta} WHERE id = #{id}")
    int updateCurrentPeople(Long id, Integer delta);
    
    /**
     * 删除搭伙活动
     */
    @Delete("DELETE FROM meal_events WHERE id = #{id}")
    int delete(Long id);
}
