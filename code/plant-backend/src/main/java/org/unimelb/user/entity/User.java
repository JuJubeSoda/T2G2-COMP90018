package org.unimelb.user.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    private String phone;

    private String password;

    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String userType;

    private String avatar;

    private byte[] avatarData;
}