package org.unimelb.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.unimelb.common.constant.ResultConstant;
import org.unimelb.common.utils.JwtUtil;
import org.unimelb.common.vo.Result;
import org.unimelb.user.entity.User;
import org.unimelb.user.mapper.UserMapper;
import org.unimelb.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {
    @Resource
    private UserMapper userMapper;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void registerUser(User user) {
        user.setUsername(user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Autowired
    private JwtUtil jwtUtil;
    @Override
    public Result<User> getUserInfo(String token) {
        try {
            User user = jwtUtil.parseJwt(token, User.class);
            Map<String, Object> data = new HashMap<>();
            return Result.success(user);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(ResultConstant.FAIL_UNLOGIN_ERROR.getCode(),ResultConstant.FAIL_UNLOGIN_ERROR.getMessage());
        }
    }

    @Override
    public String getTokenByPhone(String phone) {
        User user = userMapper.getUserByName(phone);
        if(user != null){
            return jwtUtil.createJwt(user);
        }
        return null;
    }

    @Override
    public void updateNickname(String userId, String nickname) {
        userMapper.updateNickname(userId,nickname);
    }

    @Override
    public Map<String,Object> getNewToken(String token) {
        User user = jwtUtil.parseJwt(token, User.class);
        User newUser = userMapper.selectById(user.getId());
        String newToken = jwtUtil.createJwt(newUser);
        return Map.of("user",newUser,"token",newToken);
    }


    @Override
    public Result updatePassword(Integer userId, String password, String newPassword) {
        User user = userMapper.selectById(userId);
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if(!matches){
            return Result.fail(301,"密码错误");
        }
        userMapper.updatePassword(userId,passwordEncoder.encode(newPassword));
        return Result.success();
    }

    @Override
    public void updateAvatar(Integer userId, byte[] avatar) throws SQLException {
        log.debug("avatar: " + avatar.length);
        SerialBlob avatarBlob = new SerialBlob(avatar);
        userMapper.updateAvatar(userId,avatarBlob);
    }

    @Override
    public byte[] getAvatar(Integer userId) {
        User user = userMapper.selectById(userId);
        //System.out.println(user);
        return user.getAvatarData();
    }
}
