package com.hepsiemlak.todo.mapping;

import com.hepsiemlak.todo.entity.User;
import com.hepsiemlak.todo.model.user.UserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    User toEntity(UserRequest userRequest);

}
