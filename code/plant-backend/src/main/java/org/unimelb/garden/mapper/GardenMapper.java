package org.unimelb.garden.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.unimelb.garden.entity.Garden;

import java.util.List;

@Mapper
public interface GardenMapper extends BaseMapper<Garden> {

    List<Garden> selectNearBy(@Param("latitude") Double latitude,
                              @Param("longitude") Double longitude,
                              @Param("radius") Integer radiusMeters);
}
