package org.unimelb.plant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.unimelb.common.constant.ResultConstant;
import org.unimelb.common.context.UserContext;
import org.unimelb.common.utils.JwtUtil;
import org.unimelb.common.vo.Result;
import org.unimelb.plant.entity.Plant;
import org.unimelb.plant.mapper.PlantMapper;
import org.unimelb.plant.service.PlantService;
import org.unimelb.plant.vo.PlantQuery;
import org.unimelb.user.entity.User;
import org.unimelb.user.mapper.UserMapper;

import java.util.List;

@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Page<Plant> pagePlants(PlantQuery query) {

        Page<Plant> page = new Page<>(query.getPageNo(), query.getPageSize());


        QueryWrapper<Plant> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(query.getPlantId() != null, Plant::getPlantId, query.getPlantId())
                .eq(query.getUserId() != null, Plant::getUserId, query.getUserId())
                .like(query.getImage() != null, Plant::getImage, query.getImage())
                .like(StringUtils.hasText(query.getDescription()), Plant::getDescription, query.getDescription())
                .eq(StringUtils.hasText(query.getPlantCategory()), Plant::getPlantCategory, query.getPlantCategory())
                .eq(query.getLatitude() != null, Plant::getLatitude, query.getLatitude())
                .eq(query.getLongitude() != null, Plant::getLongitude, query.getLongitude())
                .orderByDesc(Plant::getCreatedAt);


        return this.baseMapper.selectPage(page, qw);
    }


    @Override
    public List<Plant> listPlantsByUser() {

        Long userId = UserContext.getCurrentUserId();
        List<Plant> list = this.list(
                Wrappers.<Plant>lambdaQuery()
                        .eq(Plant::getUserId, userId)
                        .orderByDesc(Plant::getCreatedAt)
        );
        return list;
    }

    @Override
    public Plant addPlant(Plant plant) {
        Long userId = UserContext.getCurrentUserId();
        plant.setUserId(userId);
        plant.setPlantId(null);

        boolean ok = this.save(plant);
        return ok ? plant : null;
    }

}