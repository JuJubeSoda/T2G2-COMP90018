package org.unimelb.base.entity;

import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;

@Data
@TableName("base_param")
public class BaseParam implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String baseName;
    private String paramName;
    private String paramValue;
    private String paramDesc;
    private Integer priority;
}