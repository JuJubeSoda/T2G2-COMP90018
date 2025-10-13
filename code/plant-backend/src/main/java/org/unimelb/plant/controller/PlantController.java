package org.unimelb.plant.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.annotation.Resource;
import org.unimelb.common.context.UserContext;
import org.unimelb.common.vo.Result;
import org.unimelb.garden.entity.Garden;
import org.unimelb.plant.entity.Plant;
import org.unimelb.plant.service.PlantService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    @Resource
    private PlantService plantService;

    @Operation(summary = "Search plants by User Id")
    @GetMapping("/by-user")
    public Result<List<Plant>> listByUser() {

        List<Plant> list= plantService.listPlantsByUser();

        return Result.success(list);
    }

    @Operation(summary = "Search plants by Garden Id")
    @GetMapping("/by-garden")
    public Result<List<Plant>> listByGarden(@RequestParam Long gardenId) {
        //TODO
        List<Plant> list= plantService.listPlantsByGarden(gardenId);

        return Result.success(list);
    }


    @Operation(summary = "Get plants by id")
    @GetMapping("/{id}")
    public Result<Plant> getPlant(@PathVariable Long id) {
        Optional<Plant> plant = plantService.getOptById(id);
        return plant.map(Result::success).orElseGet(() -> Result.fail(500, "plant not found"));
    }

    @Operation(summary = "Get nearby plants")
    @GetMapping("/nearby")
    public Result<List<Plant>> getNearbyPlants(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(name = "radius", defaultValue = "1000") int radius) {
        return Result.success(plantService.getNearByPlants(latitude, longitude, radius));
    }

    /**
     * Add a plant via JSON body.
     * POST /api/plants
     * Content-Type: application/json
     */
    @Operation(summary = "Add New Plant")
    @PostMapping("/add")
    public Result<Plant> addPlant(@RequestBody Plant plant) {
        Plant plantSaved=plantService.addPlant(plant);
        return plantSaved==null ? Result.fail(500, "save failed"):Result.success(plantSaved);
    }

    @Operation(summary = "Like a plant")
    @PostMapping("/like")
    public Result<?> like(@RequestParam Long plantId) {
        Long userId = UserContext.getCurrentUserId();
        boolean ok = plantService.like(userId, plantId);
        return ok ? Result.success("liked") : Result.fail(500, "like failed");
    }

    @Operation(summary = "Unlike a plant")
    @PostMapping("/unlike")
    public Result<?> unlike(@RequestParam Long plantId) {
        Long userId = UserContext.getCurrentUserId();
        boolean ok = plantService.unlike(userId, plantId);
        return ok ? Result.success("unliked") : Result.fail(500, "unlike failed");
    }


}
