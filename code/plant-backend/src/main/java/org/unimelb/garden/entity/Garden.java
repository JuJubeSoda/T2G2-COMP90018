package org.unimelb.garden.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("garden")
public class Garden implements Serializable {
    /**
     * garden ID
     */
    private Long gardenId;

    /**
     * latitude
     */
    private Double latitude;

    /**
     * longitude
     */
    private Double longitude;

    /**
     * name
     */
    private String name;

    /**
     * description
     */
    private String description;
}
