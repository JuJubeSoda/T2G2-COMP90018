package org.unimelb.plant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.common.vo.Result;
import org.unimelb.plant.entity.Plant;
import org.unimelb.plant.vo.PlantQuery;
import org.unimelb.user.entity.User;

import java.util.List;

public interface PlantService extends IService<Plant> {
    public Page<Plant> pagePlants(PlantQuery query);

    List<Plant> listPlantsByUser();

    Plant addPlant(Plant plant);

}
