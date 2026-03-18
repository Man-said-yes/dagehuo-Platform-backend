package com.gdmu.mapper;

import com.gdmu.pojo.SystemNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 系统通知Mapper接口
 */
@Mapper
public interface SystemNotificationMapper {
    // 插入系统通知
    @Insert("INSERT INTO system_notification(user_id, activity_id, notification_type, title, content) VALUES(#{userId}, #{activityId}, #{notificationType}, #{title}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SystemNotification notification);

    // 根据用户ID查询系统通知列表（按创建时间倒序）
    @Select("SELECT * FROM system_notification WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<SystemNotification> selectByUserId(@Param("userId") Long userId);

    // 根据用户ID和通知类型查询系统通知列表
    @Select("SELECT * FROM system_notification WHERE user_id = #{userId} AND notification_type = #{notificationType} ORDER BY create_time DESC")
    List<SystemNotification> selectByUserIdAndType(@Param("userId") Long userId, @Param("notificationType") Integer notificationType);

    // 根据用户ID查询未读系统通知数量
    @Select("SELECT COUNT(*) FROM system_notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    // 更新通知为已读
    @Update("UPDATE system_notification SET is_read = 1 WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    // 批量更新通知为已读
    @Update("<script>" +
            "UPDATE system_notification SET is_read = 1 " +
            "WHERE user_id = #{userId} AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int markMultipleAsRead(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    // 根据活动ID查询通知
    @Select("SELECT * FROM system_notification WHERE activity_id = #{activityId}")
    List<SystemNotification> selectByActivityId(@Param("activityId") Long activityId);
}