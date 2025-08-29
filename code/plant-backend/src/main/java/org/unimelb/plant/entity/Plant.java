package org.unimelb.plant.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@TableName("plant")
public class Plant implements Serializable{
    @TableId(type = IdType.AUTO)
    private Long plantId;
    private Long userId;
    private String imageUrl;
    private String description;
    private String location;
    private String plantCategory;
    private LocalDate createdAt;
}
