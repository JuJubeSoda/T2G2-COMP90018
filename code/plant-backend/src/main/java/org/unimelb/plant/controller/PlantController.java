package org.unimelb.plant.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    // 新增
    @GetMapping("/by-user")
    public Result<List<Plant>> listByUser(@RequestHeader("Authorization") String token) {
        return plantService.listPlantsByUser(token);
    }

    /**
     * Add a plant via JSON body.
     * POST /api/plants
     * Content-Type: application/json
     */
    @PostMapping("/add")
    public Result<Plant> addPlant(@RequestHeader("Authorization") String token, @RequestBody Plant plant) {
        if (token == null || token.isBlank()) {
            return Result.fail(400, "token is required");
        }
        return plantService.addPlant(token, plant);
    }


}
