package org.unimelb.garden.service;

import org.unimelb.common.vo.Result;
import org.unimelb.garden.entity.Garden;
import org.unimelb.plant.entity.Plant;

import java.util.List;

public interface GardenService {

    /**
     * getAllGardens
     * @return List<Garden>
     */
    List<Garden> getAllGardens();

    /**
     * addGarden
     * @param garden
     * @return Result<Garden>
     */
    Boolean addGarden(Garden garden);

    /**
     * deleteGarden
     * @param gardenId
     * @return Result<Garden>
     */
    Boolean deleteGarden(Integer gardenId);

    /**
     * updateGarden
     * @param garden
     * @return Result<Garden>
     */
    Boolean updateGarden(Garden garden);

    /**
     * getGardenById
     * @param gardenId
     * @return Result<Garden>
     */
    Garden getGardenById(Long gardenId);

    /**
     * get NearBy Gardens
     * @param
     * @return Result<Garden>
     */
    List<Garden> getNearByGardens(Double latitude, Double longitude, int radius);

    /**
     * get Plants In Garden
     * @param
     * @return Result<Garden>
     */
    List<Plant> getPlantsInGarden(Long gardenId);

}
