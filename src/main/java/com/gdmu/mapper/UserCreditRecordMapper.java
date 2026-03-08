package com.gdmu.mapper;

import com.gdmu.pojo.UserCreditRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserCreditRecordMapper {
    // 插入信誉分记录
    @Insert("INSERT INTO user_credit_record(user_id, credit_change, reason) VALUES(#{userId}, #{creditChange}, #{reason})")
    int insert(UserCreditRecord record);

    // 根据用户ID查询信誉分记录
    @Select("SELECT * FROM user_credit_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<UserCreditRecord> selectByUserId(@Param("userId") Long userId);
}