//package com.qqcn.sms.controller;
//
//
//import com.qqcn.common.vo.Result;
//import com.qqcn.sms.service.SmsService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//
//@Tag(name = "短信模块",description = "短信处理相关接口")
//@RestController
//public class SmsController {
//    @Autowired
//    private SmsService smsService;
//
//    // 1. 发送验证码
//    @Operation(summary = "发送验证码")
//    @GetMapping("/sms/{phone}")
//    public Result<Object> sendSms(@PathVariable("phone") String phone){
//        smsService.sendSmsCapthcha(phone);
//        return Result.success();
//    }
//
//    // 2. 校验验证码
//    @Operation(summary = "校验验证码")
//    @GetMapping("/sms/check")
//    public Result<Boolean> checkCapthcha(@RequestParam("phone") String phone, @RequestParam("code") String code){
//        boolean isValid = smsService.checkCapthcha(phone,code);
//        return Result.success(isValid);
//    }
//}
