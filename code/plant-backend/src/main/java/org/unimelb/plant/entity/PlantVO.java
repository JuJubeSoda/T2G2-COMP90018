package org.unimelb.plant.entity;
import lombok.Data;
import java.io.Serializable;


@Data
public class PlantVO implements Serializable {

    private Long plantId;

    private Long userId;

    private String name;

    private String description;

    private Double latitude;

    private Double longitude;

    private String scientificName;

    private Long gardenId;
    
    private String discoveredBy;

}
