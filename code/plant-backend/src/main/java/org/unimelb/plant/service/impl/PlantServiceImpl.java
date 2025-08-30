package org.unimelb.plant.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.unimelb.common.constant.ResultConstant;
import org.unimelb.common.utils.JwtUtil;
import org.unimelb.common.vo.Result;
import org.unimelb.plant.entity.Plant;
import org.unimelb.plant.mapper.PlantMapper;
import org.unimelb.plant.service.PlantService;
import org.unimelb.plant.vo.PlantQuery;
import org.unimelb.user.entity.User;
import org.unimelb.user.mapper.UserMapper;

import java.util.List;

@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Page<Plant> pagePlants(PlantQuery query) {

        Page<Plant> page = new Page<>(query.getPageNo(), query.getPageSize());


        QueryWrapper<Plant> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(query.getPlantId() != null, Plant::getPlantId, query.getPlantId())
                .eq(query.getUserId()  != null, Plant::getUserId,  query.getUserId())
                .like(StringUtils.hasText(query.getImageURL()),     Plant::getImageUrl,     query.getImageURL())
                .like(StringUtils.hasText(query.getDescription()),  Plant::getDescription,  query.getDescription())
                .like(StringUtils.hasText(query.getLocation()),     Plant::getLocation,     query.getLocation())
                .eq(StringUtils.hasText(query.getPlantCategory()),  Plant::getPlantCategory, query.getPlantCategory())
                .orderByDesc(Plant::getCreatedAt);


        return this.baseMapper.selectPage(page, qw);
    }



    @Override
    public Result<List<Plant>> listPlantsByUser(String token) {
        try {
            String raw = normalizeToken(token);
            User user = jwtUtil.parseJwt(raw, User.class);
            if (user == null || user.getId() == null) {
                return Result.fail(ResultConstant.FAIL_UNLOGIN_ERROR.getCode(),
                        ResultConstant.FAIL_UNLOGIN_ERROR.getMessage());
            }

            List<Plant> list = this.list(
                    Wrappers.<Plant>lambdaQuery()
                            .eq(Plant::getUserId, user.getId())
                            .orderByDesc(Plant::getCreatedAt)
            );
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(ResultConstant.FAIL_UNLOGIN_ERROR.getCode(),
                    ResultConstant.FAIL_UNLOGIN_ERROR.getMessage());
        }
    }

    @Override
    public Result<Plant> addPlant(String token, Plant plant) {
        try {
            String raw = normalizeToken(token);
            User user = jwtUtil.parseJwt(raw, User.class);
            if (user == null || user.getId() == null) {
                return Result.fail(ResultConstant.FAIL_UNLOGIN_ERROR.getCode(),
                        ResultConstant.FAIL_UNLOGIN_ERROR.getMessage());
            }

            plant.setUserId(Long.valueOf(user.getId()));
            plant.setPlantId(null); // 自增

            boolean ok = this.save(plant);
            return ok ? Result.success(plant) : Result.fail(500, "save failed");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(ResultConstant.FAIL_UNLOGIN_ERROR.getCode(),
                    ResultConstant.FAIL_UNLOGIN_ERROR.getMessage());
        }
    }


    private String normalizeToken(String token) {
        if (!StringUtils.hasText(token)) return token;
        String t = token.trim();
        return (t.regionMatches(true, 0, "Bearer ", 0, 7)) ? t.substring(7).trim() : t;
    }


}