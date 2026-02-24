package com.gdmu.mapper;

import com.gdmu.pojo.MealEvent;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MealEventMapper {
    // 创建活动
    @Insert("INSERT INTO meal_events(title, description, event_time, location, max_people, current_people, status, type, creator_id) VALUES(#{title}, #{description}, #{eventTime}, #{location}, #{maxPeople}, #{currentPeople}, #{status}, #{type}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MealEvent mealEvent);

    // 根据ID查询活动
    @Select("SELECT * FROM meal_events WHERE id = #{id}")
    MealEvent selectById(Long id);

    // 查询所有活动（按创建时间倒序）
    @Select("SELECT * FROM meal_events ORDER BY create_time DESC")
    List<MealEvent> selectAll();

    // 根据类型查询活动
    @Select("SELECT * FROM meal_events WHERE type = #{type} ORDER BY create_time DESC")
    List<MealEvent> selectByType(Integer type);

    // 根据创建者ID查询活动
    @Select("SELECT * FROM meal_events WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<MealEvent> selectByCreatorId(Long creatorId);

    // 更新活动信息
    @Update("UPDATE meal_events SET title = #{title}, description = #{description}, event_time = #{eventTime}, location = #{location}, max_people = #{maxPeople}, status = #{status}, type = #{type} WHERE id = #{id}")
    int update(MealEvent mealEvent);

    // 更新活动状态
    @Update("UPDATE meal_events SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 更新当前参与人数
    @Update("UPDATE meal_events SET current_people = #{currentPeople} WHERE id = #{id}")
    int updateCurrentPeople(@Param("id") Long id, @Param("currentPeople") Integer currentPeople);

    // 删除活动
    @Delete("DELETE FROM meal_events WHERE id = #{id}")
    int deleteById(Long id);
}