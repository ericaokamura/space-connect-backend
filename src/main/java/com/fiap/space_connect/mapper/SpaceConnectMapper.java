package com.fiap.space_connect.mapper;

import com.fiap.space_connect.model.SpaceObject;
import com.fiap.space_connect.model.dto.SpaceObjectDTO;

public class SpaceConnectMapper {

    public static SpaceObjectDTO convertModelToDTO(SpaceObject spaceObject) {
        SpaceObjectDTO spaceObjectDTO = new SpaceObjectDTO();
        spaceObjectDTO.setNoradId(spaceObject.getNoradId());
        spaceObjectDTO.setObjectName(spaceObject.getObjectName());
        spaceObjectDTO.setPositionX(spaceObject.getPositionX());
        spaceObjectDTO.setPositionY(spaceObject.getPositionY());
        spaceObjectDTO.setPositionZ(spaceObject.getPositionZ());
        return spaceObjectDTO;
    }
    public static SpaceObject convertDTOToModel(SpaceObjectDTO dto) {
        SpaceObject model = new SpaceObject();
        model.setNoradId(dto.getNoradId());
        model.setObjectName(dto.getObjectName());
        model.setPositionX(dto.getPositionX());
        model.setPositionY(dto.getPositionY());
        model.setPositionZ(dto.getPositionZ());
        return model;
    }

}
