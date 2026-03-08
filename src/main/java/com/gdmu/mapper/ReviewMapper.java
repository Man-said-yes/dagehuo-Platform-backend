package com.gdmu.mapper;

import com.gdmu.pojo.Review;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 评价Mapper接口
 */
@Mapper
public interface ReviewMapper {
    
    /**
     * 发布评价
     */
    @Insert("INSERT INTO reviews(activity_id, reviewer_id, reviewed_id, rating, content, create_time) VALUES(#{mealEventId}, #{reviewerId}, #{reviewedId}, #{rating}, #{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Review review);
    
    /**
     * 根据ID查询评价
     */
    @Select("SELECT * FROM reviews WHERE id = #{id}")
    Review selectById(Long id);
    
    /**
     * 根据活动ID查询评价
     */
    @Select("SELECT * FROM reviews WHERE activity_id = #{activityId}")
    List<Review> selectByActivityId(Long activityId);
    
    /**
     * 根据被评价者ID查询评价
     */
    @Select("SELECT * FROM reviews WHERE reviewed_id = #{reviewedId}")
    List<Review> selectByReviewedId(Long reviewedId);
    
    /**
     * 查询用户获得的平均评分
     */
    @Select("SELECT AVG(rating) FROM reviews WHERE reviewed_id = #{reviewedId}")
    Double selectAverageRating(Long reviewedId);
}
