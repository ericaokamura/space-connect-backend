package com.fiap.space_connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpaceObjectDTO {

    private String noradId;

    private String objectName;

    private double positionX;

    private double positionY;

    private double positionZ;

}
