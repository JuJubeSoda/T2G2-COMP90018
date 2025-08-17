package org.unimelb.user.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;

@Data
@TableName("user")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String nickname;
    private String phone;
    private String password;
    private String email;
    private String wechat;
    private java.util.Date fcd;
    private java.util.Date lud;
    private String userType;
    private String avatar;
    private byte[] avatarData;

}