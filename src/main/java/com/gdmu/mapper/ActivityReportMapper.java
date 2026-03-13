package com.gdmu.mapper;

import com.gdmu.pojo.ActivityReport;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 活动举报Mapper接口
 */
@Mapper
public interface ActivityReportMapper {
    // 插入举报记录
    @Insert("INSERT INTO activity_report(activity_id, reporter_user_id, report_reason) VALUES(#{activityId}, #{reporterUserId}, #{reportReason})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ActivityReport report);

    // 根据活动ID查询举报记录
    @Select("SELECT * FROM activity_report WHERE activity_id = #{activityId}")
    List<ActivityReport> selectByActivityId(@Param("activityId") Long activityId);

    // 查询所有举报记录（支持分页）
    @Select({"<script>",
            "SELECT * FROM activity_report",
            "ORDER BY report_time DESC",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"})
    List<ActivityReport> selectAllWithPagination(@Param("offset") int offset, @Param("pageSize") int pageSize);

    // 查询举报记录总数
    @Select("SELECT COUNT(*) FROM activity_report")
    int countReports();

    // 更新举报处理状态
    @Update("UPDATE activity_report SET handle_status = #{handleStatus}, handle_time = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateHandleStatus(@Param("id") Long id, @Param("handleStatus") Integer handleStatus);

    // 根据处理状态查询举报记录
    @Select({"<script>",
            "SELECT * FROM activity_report",
            "WHERE handle_status = #{handleStatus}",
            "ORDER BY report_time DESC",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"})
    List<ActivityReport> selectByHandleStatus(@Param("handleStatus") Integer handleStatus, @Param("offset") int offset, @Param("pageSize") int pageSize);

    // 根据处理状态查询举报记录总数
    @Select("SELECT COUNT(*) FROM activity_report WHERE handle_status = #{handleStatus}")
    int countReportsByHandleStatus(@Param("handleStatus") Integer handleStatus);
    
    // 根据ID查询举报记录
    @Select("SELECT * FROM activity_report WHERE id = #{id}")
    ActivityReport selectById(@Param("id") Long id);
    
    // 根据活动ID更新所有举报记录的处理状态
    @Update("UPDATE activity_report SET handle_status = #{handleStatus}, handle_time = CURRENT_TIMESTAMP WHERE activity_id = #{activityId}")
    int updateHandleStatusByActivityId(@Param("activityId") Long activityId, @Param("handleStatus") Integer handleStatus);
    
    // 查询未建议过的举报记录（ai_suggested = 0）
    @Select("SELECT * FROM activity_report WHERE ai_suggested = 0")
    List<ActivityReport> selectUnsuggestedReports();
    
    // 更新AI建议字段
    @Update("UPDATE activity_report SET ai_suggestion = #{aiSuggestion}, ai_suggested = 1 WHERE id = #{id}")
    int updateAiSuggestion(@Param("id") Long id, @Param("aiSuggestion") Integer aiSuggestion);
    
    // 查询已建议但未处理的举报记录（ai_suggested = 1 AND handle_status = 0）
    @Select("SELECT * FROM activity_report WHERE ai_suggested = 1 AND handle_status = 0")
    List<ActivityReport> selectSuggestedButUnprocessedReports();
}