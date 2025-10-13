package org.unimelb.wiki.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("plant_wiki")
public class PlantWiki {


    private Long plantWikiId;

    private String name;

    private String scientificName;

    private byte[] image;

    private String description;

    private String features;

    private String careGuide;

    private String waterNeeds;

    private String lightNeeds;

    private String difficulty;

    private String GrowthHeight;
}
