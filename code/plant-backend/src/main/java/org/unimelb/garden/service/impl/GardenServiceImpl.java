package org.unimelb.garden.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.unimelb.garden.entity.Garden;
import org.unimelb.garden.mapper.GardenMapper;
import org.unimelb.garden.service.GardenService;
import org.unimelb.plant.entity.Plant;

import java.util.List;

@Service
public class GardenServiceImpl implements GardenService {

    @Resource
    private GardenMapper gardenMapper;

    @Override
    public List<Garden> getAllGardens() {
        return gardenMapper.selectList(Wrappers.lambdaQuery());
    }

    @Override
    public Boolean addGarden(Garden garden) {
        return gardenMapper.insert(garden) > 0;
    }

    @Override
    public Boolean batchAddGardens(List<Garden> gardenList) {
        return gardenMapper.insertBatch(gardenList) > 0;
    }

    @Override
    public Boolean deleteGarden(Integer gardenId) {
        return null;
    }

    @Override
    public Boolean updateGarden(Garden garden) {
        return null;
    }

    @Override
    public Garden getGardenById(Long gardenId) {
        return null;
    }

    @Override
    public List<Garden> getNearByGardens(Double latitude, Double longitude, int radius) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("latitude/longitude cannot be null");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("illegal longitude or latitude");
        }
        return gardenMapper.selectNearBy(latitude, longitude, radius);
    }

    @Override
    public List<Plant> getPlantsInGarden(Long gardenId) {
        return List.of();
    }


}
