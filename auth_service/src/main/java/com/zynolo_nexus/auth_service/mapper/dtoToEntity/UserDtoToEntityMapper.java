package com.zynolo_nexus.auth_service.mapper.dtoToEntity;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.zynolo_nexus.auth_service.dto.response.ProfileDetails;
import com.zynolo_nexus.auth_service.model.User;

@Component
public class UserDtoToEntityMapper {

    private final ModelMapper modelMapper;

    public UserDtoToEntityMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public User toEntity(ProfileDetails dto) {
        return modelMapper.map(dto, User.class);
    }
}
