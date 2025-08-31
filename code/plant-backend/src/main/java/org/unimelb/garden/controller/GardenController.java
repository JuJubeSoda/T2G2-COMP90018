package org.unimelb.garden.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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



}
