
package com.gdmu.mapper;
import com.gdmu.pojo.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    // 根据openid查询用户
    @Select("SELECT * FROM users WHERE openid = #{openid}")
    User selectByOpenid(String openid);
    
    // 根据id查询用户
    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(Long id);
    
    // 检查学号是否已存在
    @Select("SELECT COUNT(*) FROM users WHERE student_id = #{studentId}")
    int existsByStudentId(String studentId);
    
    // 创建用户
    @Insert("INSERT INTO users(openid, nickname) VALUES(#{openid}, #{nickname})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    // 更新用户信息
    @Update("UPDATE users SET student_id = #{studentId}, nickname = #{nickname}, avatar = #{avatar}, gender = #{gender}, phone = #{phone} WHERE id = #{id}")
    int update(User user);
    
    // 根据学号查询用户
    @Select("SELECT * FROM users WHERE student_id = #{studentId}")
    User selectByStudentId(String studentId);
}