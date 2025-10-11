package org.unimelb.wiki.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.unimelb.wiki.entity.PlantWiki;

@Mapper
public interface WikiMapper extends BaseMapper<PlantWiki> {

}
