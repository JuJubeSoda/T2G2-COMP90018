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

    private Double temperature;

    private Double humidity;

    private String waterNeeds;

    private String lightNeeds;

    private String soil;

    private String fertilizing;

    private String leafType;

    private String Toxicity;

    private String airPurifying;
}
