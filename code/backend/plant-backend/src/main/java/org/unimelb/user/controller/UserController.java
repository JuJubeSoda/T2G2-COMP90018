package org.unimelb.user.controller;

import org.unimelb.common.vo.Result;
import org.unimelb.user.entity.User;
import org.unimelb.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Tag(name = "用户模块",description = "用户模块接口")
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/reg")
    public Result<?> registerUser(@RequestBody User user){
        userService.registerUser(user);
        return Result.success("Register success");
    }

    @Secured({"ROLE_admin"})
    @Operation(summary = "查询所有用户")
    @GetMapping
    public Result<List<User>> getAllUser(){
        List<User> list = userService.list();
        return Result.success(list);
    }

    @Operation(summary ="获取用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token){
        return userService.getUserInfo(token);
    }


//    @Autowired
//    private SmsService smsService;
//
//    @Operation(summary = "短信验证码登录")
//    @PostMapping("/sms/login")
//    public Result<?> smsLogin(@RequestBody Map map){
//        Boolean check = smsService.checkCapthcha(map.get("phone") + "", map.get("captcha") + "");
//        if(!check){
//            return Result.fail(ResultConstant.FAIL.getCode(),"验证码错误");
//        }
//        String token = userService.getTokenByPhone(map.get("phone") + "");
//        return Result.success(token,"登录成功");
//    }


    @Operation(summary = "获取新token")
    @GetMapping("/token")
    public Result<Map<String,Object>> getNewToken(@RequestHeader("Authorization") String token){
        Map<String,Object> result = userService.getNewToken(token);
        return Result.success(result);
    }

    @Operation(summary = "修改昵称")
    @PutMapping("/nickname")
    public Result<?> updateNickname(@RequestParam("userId") String userId,@RequestParam("nickname") String nickname){
        userService.updateNickname(userId,nickname);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map param){

        Result result
                = userService.updatePassword(Integer.parseInt(param.get("userId")+""),param.get("password")+"",param.get("newPassword")+"");
        return result;
    }


    @Operation(summary = "修改头像")
    @PostMapping("/avatar")
    public Result<?> updateAvatar(MultipartFile file, @RequestParam("userId") Integer userId) throws IOException, SQLException {

        // 读取上传的原始图片
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // 创建缩略图
        int thumbnailSize = 200;

        // 计算缩略图的宽度和高度，保持原宽高比例
        int newWidth, newHeight;
        if (originalImage.getWidth() > originalImage.getHeight()) {
            newWidth = thumbnailSize;
            newHeight = thumbnailSize * originalImage.getHeight() / originalImage.getWidth();
        } else {
            newWidth = thumbnailSize * originalImage.getWidth() / originalImage.getHeight();
            newHeight = thumbnailSize;
        }

        // 创建缩略图
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbnail.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics2D.dispose();

        // 裁剪成以图片中心为中心的正方形，因为前端是以正方形显示
        int x = 0;
        int y = 0;
        int cropSize = Math.min(newWidth, newHeight);
        if (newWidth > newHeight) {
            x = (newWidth - cropSize) / 2;
        } else {
            y = (newHeight - cropSize) / 2;
        }
        thumbnail = thumbnail.getSubimage(x, y, cropSize, cropSize);



        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", bs);
        byte[] thumbnailBytes = bs.toByteArray();

        userService.updateAvatar(userId,thumbnailBytes);
        return Result.success();
    }



    @Operation(summary = "查询头像")
    @GetMapping("/avatar")
    public Result<?> getAvatar(@RequestParam("userId") Integer userId){
        byte[] arvatarData =  userService.getAvatar(userId);
        return Result.success(arvatarData);
    }
}
