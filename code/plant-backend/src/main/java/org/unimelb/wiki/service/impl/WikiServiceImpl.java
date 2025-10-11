package org.unimelb.wiki.service.impl;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unimelb.wiki.entity.PlantWiki;
import org.unimelb.wiki.mapper.WikiMapper;
import org.unimelb.wiki.service.WikiService;

import java.util.List;

@Service
public class WikiServiceImpl implements WikiService {
    @Resource
    private WikiMapper wikiMapper;
    @Override
    public List<PlantWiki> getAllWikis() {
        return wikiMapper.selectList(Wrappers.lambdaQuery());
    }


}
