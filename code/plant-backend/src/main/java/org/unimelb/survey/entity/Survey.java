package org.unimelb.survey.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("wj_survey")
public class Survey implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String description;
    private Integer status;
    private Integer answerTotal;
    private Integer star;
    private Integer userId;
    private java.util.Date fcd;
    private java.util.Date lud;
    private Integer timeLimit;
    private Integer deleted;

    @TableField(exist = false)
    private List<?> examineeList = new ArrayList<>();
    @TableField(exist = false)
    private List<?> questionList = new ArrayList<>();
}