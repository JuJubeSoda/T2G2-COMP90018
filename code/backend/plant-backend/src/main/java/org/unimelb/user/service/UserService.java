package org.unimelb.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.common.vo.Result;
import org.unimelb.user.entity.User;

import java.sql.SQLException;
import java.util.Map;

public interface UserService extends IService<User> {
    public void registerUser(User user);

    Result<User> getUserInfo(String token);

    String getTokenByPhone(String phone);

    void updateNickname(String userId, String nickname);

    Map<String,Object> getNewToken(String token);

    Result updatePassword(Integer userId, String password, String newPassword);

    void updateAvatar(Integer userId, byte[] bytes) throws SQLException;

    byte[] getAvatar(Integer userId);
}
