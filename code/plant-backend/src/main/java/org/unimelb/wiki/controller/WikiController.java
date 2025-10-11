package org.unimelb.wiki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.unimelb.common.vo.Result;
import org.unimelb.garden.entity.Garden;
import org.unimelb.wiki.entity.PlantWiki;
import org.unimelb.wiki.service.WikiService;

import java.util.List;

@Controller
public class WikiController {

    @Autowired
    private WikiService wikiService;

    public Result<List<PlantWiki>> getAllWikis() {
        return Result.success(wikiService.getAllWikis());
    }
}
