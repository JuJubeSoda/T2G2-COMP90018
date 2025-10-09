package org.unimelb.plant.vo;

import lombok.EqualsAndHashCode;
import org.unimelb.common.vo.MyPage;
import lombok.Data;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlantQuery extends MyPage{
    private String name;
    private Long plantId;
    private Long userId;
    private byte[] image;
    private String description;
    private Double latitude;
    private Double longitude;
    private String scientificName;

}
