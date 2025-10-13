package org.unimelb.wiki.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unimelb.common.vo.Result;
import org.unimelb.garden.entity.Garden;
import org.unimelb.wiki.entity.PlantWiki;
import org.unimelb.wiki.service.WikiService;

import java.util.List;

@RestController
@RequestMapping("api/wiki")
public class WikiController {

    @Autowired
    private WikiService wikiService;

    @Operation(summary = "Get all wikis")
    @GetMapping("/all")
    public Result<List<PlantWiki>> getAllWikis() {
        return Result.success(wikiService.getAllWikis());
    }
}
