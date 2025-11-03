package org.unimelb.plant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.unimelb.common.constant.ResultConstant;
import org.unimelb.common.context.UserContext;
import org.unimelb.common.utils.JwtUtil;
import org.unimelb.common.vo.Result;
import org.unimelb.plant.entity.Plant;
import org.unimelb.plant.entity.UserPlantLike;
import org.unimelb.plant.mapper.PlantMapper;
import org.unimelb.plant.mapper.UserPlantLikeMapper;
import org.unimelb.plant.service.PlantService;
import org.unimelb.plant.vo.PlantQuery;
import org.unimelb.user.entity.User;
import org.unimelb.user.mapper.UserMapper;

import java.util.List;

@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PlantMapper plantMapper;

    @Autowired
    private UserPlantLikeMapper userPlantLikeMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Page<Plant> pagePlants(PlantQuery query) {

        Page<Plant> page = new Page<>(query.getPageNo(), query.getPageSize());


        QueryWrapper<Plant> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(query.getPlantId() != null, Plant::getPlantId, query.getPlantId())
                .eq(query.getUserId() != null, Plant::getUserId, query.getUserId())
                .like(query.getImage() != null, Plant::getImage, query.getImage())
                .like(StringUtils.hasText(query.getDescription()), Plant::getDescription, query.getDescription())
                .eq(StringUtils.hasText(query.getScientificName()), Plant::getScientificName, query.getScientificName())
                .eq(query.getLatitude() != null, Plant::getLatitude, query.getLatitude())
                .eq(query.getLongitude() != null, Plant::getLongitude, query.getLongitude())
                .orderByDesc(Plant::getCreatedAt);


        return this.baseMapper.selectPage(page, qw);
    }

    @Override
    public List<Plant> getAllPlants() {
        return plantMapper.selectList(Wrappers.lambdaQuery());
    }


    @Override
    public List<Plant> listPlantsByUser() {

        Long userId = UserContext.getCurrentUserId();

        List<Plant> list = this.list(
                Wrappers.<Plant>lambdaQuery()
                        .eq(Plant::getUserId, userId)
                        .orderByDesc(Plant::getCreatedAt)
        );

        List<Long> likedIds = userPlantLikeMapper.selectList(
                Wrappers.<UserPlantLike>lambdaQuery().eq(UserPlantLike::getUserId, userId)
        ).stream().map(UserPlantLike::getPlantId).toList();

        // 拼接 isFavourite 字段
        list.forEach(p -> p.setIsFavourite(likedIds.contains(p.getPlantId())));

        // 拼接 discoveredBy 字段
        list.forEach(p -> {
            if (p.getUserId() != null) {
                User user = userMapper.selectById(p.getUserId());
                if (user != null && user.getUsername() != null) {
                    p.setDiscoveredBy(user.getUsername());
                }
            }
        });

        return list;
    }

    @Override
    public List<Plant> listLikedPlantsByUser() {

        Long userId = UserContext.getCurrentUserId();
        List<Long> likedPlantIds = userPlantLikeMapper.selectList(
                        Wrappers.<UserPlantLike>lambdaQuery().eq(UserPlantLike::getUserId, userId)
                                .select(UserPlantLike::getPlantId) // 只查 plantId 字段
                ).stream()
                .map(UserPlantLike::getPlantId)
                .distinct()
                .toList();

        if (likedPlantIds.isEmpty()) {
            return List.of();
        }

        List<Plant> list = this.list(
                Wrappers.<Plant>lambdaQuery()
                        .in(Plant::getPlantId, likedPlantIds)
                        .orderByDesc(Plant::getCreatedAt)
        );

        // 拼接 discoveredBy 字段
        list.forEach(p -> {
            if (p.getUserId() != null) {
                User user = userMapper.selectById(p.getUserId());
                if (user != null && user.getUsername() != null) {
                    p.setDiscoveredBy(user.getUsername());
                }
            }
        });

        return list;
    }

    @Override
    public List<Plant> listPlantsByGarden(Long gardenId) {
        List<Plant> list = this.list(
                Wrappers.<Plant>lambdaQuery()
                        .eq(Plant::getGardenId, gardenId)
                        .orderByDesc(Plant::getCreatedAt)
        );
        return list;
    }

    @Override
    public Plant addPlant(Plant plant) {
        Long userId = UserContext.getCurrentUserId();
        plant.setUserId(userId);
        plant.setPlantId(null);

        boolean ok = this.save(plant);
        return ok ? plant : null;
    }

    @Override
    public List<Plant> getNearByPlants(Double latitude, Double longitude, int radius) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("latitude/longitude cannot be null");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("illegal longitude or latitude");
        }
        List<Plant> list = plantMapper.selectNearBy(latitude, longitude, radius);
        
        // 拼接 discoveredBy 字段
        list.forEach(p -> {
            if (p.getUserId() != null) {
                User user = userMapper.selectById(p.getUserId());
                if (user != null && user.getUsername() != null) {
                    p.setDiscoveredBy(user.getUsername());
                }
            }
        });
        
        return list;
    }



    @Override
    public boolean isLiked(Long userId, Long plantId) {
        Long count = userPlantLikeMapper.selectCount(
                Wrappers.<UserPlantLike>lambdaQuery()
                        .eq(UserPlantLike::getUserId, userId)
                        .eq(UserPlantLike::getPlantId, plantId)
        );
        return count != null && count > 0;
    }
    @Override
    public boolean like(Long userId, Long plantId) {
        // 幂等：已存在就视为成功
        if (isLiked(userId, plantId)) {
            return true;
        }
        UserPlantLike like = new UserPlantLike();
        like.setUserId(userId);
        like.setPlantId(plantId);
        try {
            return userPlantLikeMapper.insert(like) > 0;
        } catch (DuplicateKeyException e) {
            // 兜底：并发下命中唯一约束也当成功
            return true;
        }
    }

    @Override
    public boolean unlike(Long userId, Long plantId) {
        int rows = userPlantLikeMapper.delete(
                Wrappers.<UserPlantLike>lambdaQuery()
                        .eq(UserPlantLike::getUserId, userId)
                        .eq(UserPlantLike::getPlantId, plantId)
        );
        // 幂等：不存在也返回成功
        return rows >= 0;
    }

}