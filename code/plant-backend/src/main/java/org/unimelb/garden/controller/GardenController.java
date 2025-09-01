package org.unimelb.garden.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.unimelb.common.vo.Result;
import org.unimelb.garden.entity.Garden;
import org.unimelb.garden.service.GardenService;

import java.util.List;

@Tag(name = "garden",description = "garden interface")
@RestController
@RequestMapping("/api/garden")
public class GardenController {

    @Autowired
    private GardenService gardenService;

    @Operation(summary = "query all gardens")
    @GetMapping("/all")
    public Result<List<Garden>> getAllGardens() {
        return Result.success(gardenService.getAllGardens());
    }


    @Operation(summary = "Add New Garden")
    @PostMapping("/add")
    public Result<?> addPlant(@RequestBody Garden garden) {
        Boolean ok = gardenService.addGarden(garden);
        return ok  ? Result.success("insert garden success") : Result.fail(500, "save failed");
    }


    @Operation(summary = "Get nearby Gardens")
    @GetMapping("/nearby")
    public Result<List<Garden>> getNearbyGardens(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(name = "radius", defaultValue = "1000") int radius) {
        return Result.success(gardenService.getNearByGardens(latitude, longitude, radius));
    }
}
