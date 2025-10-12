package org.unimelb.plant.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_plant_like")
public class UserPlantLike {
    private Long id;
    private Long userId;
    private Long plantId;
    private LocalDateTime createdAt;
}
