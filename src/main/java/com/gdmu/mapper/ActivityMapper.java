package com.gdmu.mapper;

import com.gdmu.pojo.Activity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ActivityMapper {
    // 创建活动
    @Insert("INSERT INTO activity(title, description, event_time, location, campus, longitude, latitude, max_people, current_people, status, type, high_credit, creator_id) VALUES(#{title}, #{description}, #{eventTime}, #{location}, #{campus}, #{longitude}, #{latitude}, #{maxPeople}, #{currentPeople}, #{status}, #{type}, #{highCredit}, #{creatorId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Activity activity);

    // 根据ID查询活动
    @Select("SELECT * FROM activity WHERE id = #{id}")
    Activity selectById(Long id);

    // 查询所有活动（按创建时间倒序）
    @Select("SELECT * FROM activity ORDER BY create_time DESC")
    List<Activity> selectAll();

    // 查询所有活动（分页，支持排序）
    @Select({"<script>",
            "SELECT * FROM activity",
            "ORDER BY ${sortBy} ${order}",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"})
    List<Activity> selectAllWithPagination(@Param("offset") int offset, @Param("pageSize") int pageSize, @Param("sortBy") String sortBy, @Param("order") String order);

    // 根据类型查询活动
    @Select("SELECT * FROM activity WHERE type = #{type} ORDER BY create_time DESC")
    List<Activity> selectByType(Integer type);

    // 根据类型查询活动（分页，支持排序）
    @Select({"<script>",
            "SELECT * FROM activity WHERE type = #{type}",
            "ORDER BY ${sortBy} ${order}",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"})
    List<Activity> selectByTypeWithPagination(@Param("type") Integer type, @Param("offset") int offset, @Param("pageSize") int pageSize, @Param("sortBy") String sortBy, @Param("order") String order);

    // 查询活动总数
    @Select({"<script>",
            "SELECT COUNT(*) FROM activity",
            "<if test='type != null'>",
            "WHERE type = #{type}",
            "</if>",
            "</script>"})
    int countActivities(@Param("type") Integer type);

    // 根据创建者ID查询活动
    @Select("SELECT * FROM activity WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<Activity> selectByCreatorId(Long creatorId);

    // 更新活动信息
    @Update("UPDATE activity SET title = #{title}, description = #{description}, event_time = #{eventTime}, location = #{location}, campus = #{campus}, longitude = #{longitude}, latitude = #{latitude}, max_people = #{maxPeople}, status = #{status}, type = #{type}, high_credit = #{highCredit} WHERE id = #{id}")
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
    
    // 根据距离查询活动（由近及远）
    @Select({"<script>",
            "SELECT *, 6371 * acos(cos(radians(#{latitude})) * cos(radians(latitude)) * cos(radians(longitude) - radians(#{longitude})) + sin(radians(#{latitude})) * sin(radians(latitude))) AS distance",
            "FROM activity",
            "WHERE longitude IS NOT NULL AND latitude IS NOT NULL",
            "<if test='type != null'>",
            "AND type = #{type}",
            "</if>",
            "ORDER BY distance ASC",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"})
    List<Activity> selectByDistanceAsc(@Param("longitude") Double longitude, @Param("latitude") Double latitude, @Param("type") Integer type, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    // 根据距离查询活动（由远及近）
    @Select({"<script>",
            "SELECT *, 6371 * acos(cos(radians(#{latitude})) * cos(radians(latitude)) * cos(radians(longitude) - radians(#{longitude})) + sin(radians(#{latitude})) * sin(radians(latitude))) AS distance",
            "FROM activity",
            "WHERE longitude IS NOT NULL AND latitude IS NOT NULL",
            "<if test='type != null'>",
            "AND type = #{type}",
            "</if>",
            "ORDER BY distance DESC",
            "LIMIT #{offset}, #{pageSize}",
            "</script>"})
    List<Activity> selectByDistanceDesc(@Param("longitude") Double longitude, @Param("latitude") Double latitude, @Param("type") Integer type, @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    // 查询符合距离条件的活动总数
    @Select({"<script>",
            "SELECT COUNT(*)",
            "FROM activity",
            "WHERE longitude IS NOT NULL AND latitude IS NOT NULL",
            "<if test='type != null'>",
            "AND type = #{type}",
            "</if>",
            "</script>"})
    int countActivitiesByDistance(@Param("type") Integer type);
}