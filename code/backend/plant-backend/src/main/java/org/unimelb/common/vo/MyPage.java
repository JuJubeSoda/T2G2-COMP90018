package org.unimelb.common.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MyPage implements Serializable {
    private Integer pageNo;
    private Integer pageSize;

    public Integer getPageNo(){
        if(pageNo==null){
            return 1;
        }
        return pageNo;
    }
    public Integer getPageSize(){
        if(pageSize==null){
            return 10;
        }
        return pageSize;
    }
}