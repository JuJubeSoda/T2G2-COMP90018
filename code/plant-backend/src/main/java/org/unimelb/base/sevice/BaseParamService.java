package org.unimelb.base.sevice;

import com.baomidou.mybatisplus.extension.service.IService;
import org.unimelb.base.entity.BaseParam;

import java.util.List;

public interface BaseParamService extends IService<BaseParam> {
    List<BaseParam> getParamList(String baseName);
}
