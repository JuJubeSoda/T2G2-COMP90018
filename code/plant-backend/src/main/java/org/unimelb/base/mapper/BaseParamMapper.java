package org.unimelb.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.unimelb.base.entity.BaseParam;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface BaseParamMapper extends BaseMapper<BaseParam> {
    @Select("select  * from base_param where base_name = #{baseName} order by priority")
    List<BaseParam> getParamList(String baseName);
}
