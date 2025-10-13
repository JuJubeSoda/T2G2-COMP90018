package org.unimelb.plant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.common.vo.Result;
import org.unimelb.garden.entity.Garden;
import org.unimelb.plant.entity.Plant;
import org.unimelb.plant.vo.PlantQuery;
import org.unimelb.user.entity.User;

import java.util.List;

public interface PlantService extends IService<Plant> {
    public Page<Plant> pagePlants(PlantQuery query);

    List<Plant> listPlantsByUser();

    List<Plant> listPlantsByGarden(Long gardenId);

    Plant addPlant(Plant plant);

    List<Plant> getNearByPlants(Double latitude, Double longitude, int radius);

    boolean isLiked(Long userId, Long plantId);

    boolean like(Long userId, Long plantId);

    boolean unlike(Long userId, Long plantId);

}
