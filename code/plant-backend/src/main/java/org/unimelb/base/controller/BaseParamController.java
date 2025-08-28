package org.unimelb.base.controller;

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

@Tag(name = "基础参数模块", description = "基础参数模块接口")
@RestController
@RequestMapping("/base")
@Slf4j
public class BaseParamController {
    @Autowired
    private BaseParamService baseParamService;

    @Operation(summary = "查询参数列表")
    @GetMapping
    public Result<?> getParamList(@RequestParam("baseName") String baseName) {
        List<BaseParam> list = baseParamService.getParamList(baseName);
        return Result.success(list);
    }
}
