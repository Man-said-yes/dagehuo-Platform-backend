
package com.gdmu.mapper;
import com.gdmu.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    // 根据openid查询用户
    @Select("SELECT * FROM users WHERE openid = #{openid}")
    User selectByOpenid(String openid);
    // 检查学号是否已存在
    @Select("SELECT COUNT(*) FROM users WHERE student_id = #{studentId}")
    int existsByStudentId(String studentId);
    // 创建用户
    @Insert("INSERT INTO users(openid, student_id) VALUES(#{openid}, #{studentId})")
    int insert(User user);
}