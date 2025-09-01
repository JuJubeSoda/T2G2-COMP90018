package org.unimelb.plant.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("plant")
public class Plant implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long plantId;

    private Long userId;

    private byte[] image;

    private String description;

    private Double latitude;

    private Double longitude;

    private String plantCategory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long gardenId;
}
