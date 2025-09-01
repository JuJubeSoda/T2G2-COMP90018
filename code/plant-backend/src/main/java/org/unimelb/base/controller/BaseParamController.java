package org.unimelb.base.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.unimelb.base.entity.BaseParam;
import org.unimelb.base.sevice.BaseParamService;
import org.unimelb.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Hidden
@Tag(name = "Basic Arguments", description = "Basic Arguments API")
@RestController
@RequestMapping("/base")
@Slf4j
public class BaseParamController {
    @Autowired
    private BaseParamService baseParamService;

    @Operation(summary = "query arguments list")
    @GetMapping
    public Result<?> getParamList(@RequestParam("baseName") String baseName) {
        List<BaseParam> list = baseParamService.getParamList(baseName);
        return Result.success(list);
    }
}
