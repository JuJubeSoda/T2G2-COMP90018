package org.unimelb.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.unimelb.user.entity.User;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.sql.Blob;

public interface UserMapper extends BaseMapper<User> {
    @Select("select * from wj_user where username=#{username} || phone=#{username}")
    User getUserByName(String username);

    @Update("update wj_user set nickname=#{nickname} where id = #{userId}")
    void updateNickname(String userId, String nickname);

    @Update("update wj_user set password=#{password} where id = #{userId}")
    void updatePassword(Integer userId, String password);

    @Update("update wj_user set avatar_data=#{avatarBlob} where id = #{userId}")
    void updateAvatar(Integer userId, Blob avatarBlob);
}
