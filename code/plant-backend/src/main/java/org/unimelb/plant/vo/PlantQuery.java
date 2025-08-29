package org.unimelb.plant.vo;

import lombok.EqualsAndHashCode;
import org.unimelb.common.vo.MyPage;
import lombok.Data;

import java.time.LocalDate;

@Data

public class PlantQuery extends MyPage{
    private Long plantId;
    private Long userId;
    private String imageURL;
    private String description;
    private String location;
    private String plantCategory;

}
