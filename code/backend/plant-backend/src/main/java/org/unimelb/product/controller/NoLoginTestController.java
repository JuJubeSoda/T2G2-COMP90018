package org.unimelb.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unimelb.common.vo.Result;
import org.unimelb.product.service.NoLoginTestService;

@Tag(name = "无登录测试", description = "测试无需登录场景的链路连通性")
@RestController
@RequestMapping("/test")
public class NoLoginTestController {

    @Autowired
    private NoLoginTestService noLoginTestService;

    @Operation(summary ="无参获取信息")
    @GetMapping("/test/get/general")
    public Result<String> getTestNoDatabase() {
        return noLoginTestService.getTestNoDatabase();
    }


}
