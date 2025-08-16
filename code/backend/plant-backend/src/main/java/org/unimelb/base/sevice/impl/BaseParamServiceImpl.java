package org.unimelb.base.sevice.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.unimelb.base.mapper.BaseParamMapper;
import org.unimelb.base.entity.BaseParam;
import org.unimelb.base.sevice.BaseParamService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseParamServiceImpl extends ServiceImpl<BaseParamMapper, BaseParam> implements BaseParamService {

    @Override
    public List<BaseParam> getParamList(String baseName) {

        return baseMapper.getParamList(baseName);
    }
}
