package com.gdmu.mapper;

import com.gdmu.pojo.Activity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActivityMapper {
    // 创建活动
    @Insert("INSERT INTO activity(title, description, event_time, location, max_people, current_people, status, type, creator_id) VALUES(#{title}, #{description}, #{eventTime}, #{location}, #{maxPeople}, #{currentPeople}, #{status}, #{type}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Activity activity);

    // 根据ID查询活动
    @Select("SELECT * FROM activity WHERE id = #{id}")
    Activity selectById(Long id);

    // 查询所有活动（按创建时间倒序）
    @Select("SELECT * FROM activity ORDER BY create_time DESC")
    List<Activity> selectAll();

    // 根据类型查询活动
    @Select("SELECT * FROM activity WHERE type = #{type} ORDER BY create_time DESC")
    List<Activity> selectByType(Integer type);

    // 根据创建者ID查询活动
    @Select("SELECT * FROM activity WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<Activity> selectByCreatorId(Long creatorId);

    // 更新活动信息
    @Update("UPDATE activity SET title = #{title}, description = #{description}, event_time = #{eventTime}, location = #{location}, max_people = #{maxPeople}, status = #{status}, type = #{type} WHERE id = #{id}")
    int update(Activity activity);

    // 更新活动状态
    @Update("UPDATE activity SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 更新当前参与人数
    @Update("UPDATE activity SET current_people = #{currentPeople} WHERE id = #{id}")
    int updateCurrentPeople(@Param("id") Long id, @Param("currentPeople") Integer currentPeople);

    // 删除活动
    @Delete("DELETE FROM activity WHERE id = #{id}")
    int deleteById(Long id);
}