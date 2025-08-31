package org.unimelb.plant.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.annotation.Resource;
import org.unimelb.common.vo.Result;
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

        List<Plant> list= plantService.list();

        return Result.success(list);
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


}
