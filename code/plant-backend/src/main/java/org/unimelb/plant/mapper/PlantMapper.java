package org.unimelb.plant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.unimelb.plant.entity.Plant;

import java.util.List;

@Mapper
public interface PlantMapper extends BaseMapper<Plant> {

}
