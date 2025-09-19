package org.unimelb.product.service.impl;

import org.springframework.stereotype.Service;
import org.unimelb.common.vo.Result;
import org.unimelb.product.service.NoLoginTestService;

@Service
public class NoLoginTestServiceImpl implements NoLoginTestService {

    @Override
    public Result<String> getTestNoDatabase() {
        return Result.success("service ok");
    }
}
