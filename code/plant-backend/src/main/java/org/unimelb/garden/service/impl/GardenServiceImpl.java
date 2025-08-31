package org.unimelb.garden.service.impl;

import org.springframework.stereotype.Service;
import org.unimelb.garden.entity.Garden;
import org.unimelb.garden.service.GardenService;
import org.unimelb.plant.entity.Plant;

import java.util.List;

@Service
public class GardenServiceImpl implements GardenService {


    @Override
    public List<Garden> getAllGardens() {
        return List.of();
    }

    @Override
    public Boolean addGarden(Garden garden) {
        return null;
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
        return List.of();
    }

    @Override
    public List<Plant> getPlantsInGarden(Long gardenId) {
        return List.of();
    }


}
