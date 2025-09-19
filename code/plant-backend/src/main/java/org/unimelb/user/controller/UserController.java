package org.unimelb.user.controller;

import io.swagger.v3.oas.annotations.Hidden;
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

@Tag(name = "user",description = "user interface")
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "user registration")
    @PostMapping("/reg")
    public Result<?> registerUser(@RequestBody User user){
        userService.registerUser(user);
        return Result.success("Register success");
    }

    @Secured({"ROLE_admin"})
    @Operation(summary = "query all users")
    @GetMapping
    public Result<List<User>> getAllUser(){
        List<User> list = userService.list();
        return Result.success(list);
    }

    @Operation(summary ="get user info")
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token){
        return userService.getUserInfo(token);
    }


    @Operation(summary = "get new access token")
    @GetMapping("/token")
    public Result<Map<String,Object>> getNewToken(@RequestHeader("Authorization") String token){
        Map<String,Object> result = userService.getNewToken(token);
        return Result.success(result);
    }

    @Hidden
    @Operation(summary = "modify nickname")
    @PutMapping("/nickname")
    public Result<?> updateNickname(@RequestParam("userId") String userId,@RequestParam("nickname") String nickname){
        userService.updateNickname(userId,nickname);
        return Result.success();
    }

    @Operation(summary = "modify password")
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map param){

        Result result
                = userService.updatePassword(Integer.parseInt(param.get("userId")+""),param.get("password")+"",param.get("newPassword")+"");
        return result;
    }


    @Hidden
    @Operation(summary = "modify avatar")
    @PostMapping("/avatar")
    public Result<?> updateAvatar(MultipartFile file, @RequestParam("userId") Integer userId) throws IOException, SQLException {

        // load image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // create small size image
        int thumbnailSize = 200;

        // keep origin ratio
        int newWidth, newHeight;
        if (originalImage.getWidth() > originalImage.getHeight()) {
            newWidth = thumbnailSize;
            newHeight = thumbnailSize * originalImage.getHeight() / originalImage.getWidth();
        } else {
            newWidth = thumbnailSize * originalImage.getWidth() / originalImage.getHeight();
            newHeight = thumbnailSize;
        }

        // create small size image
        BufferedImage thumbnail = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = thumbnail.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics2D.dispose();

        // corp to
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



    @Hidden
    @Operation(summary = "search avatar")
    @GetMapping("/avatar")
    public Result<?> getAvatar(@RequestParam("userId") Integer userId){
        byte[] arvatarData =  userService.getAvatar(userId);
        return Result.success(arvatarData);
    }
}
