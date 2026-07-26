package com.shiptrackpro.user.mapper;

import com.shiptrackpro.user.dto.response.InternalUserResponse;
import com.shiptrackpro.user.dto.response.UserResponse;
import com.shiptrackpro.user.entity.Role;
import com.shiptrackpro.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for User entity → DTO conversions.
 *
 * Two mapping targets:
 * 1. UserResponse — public API (no passwordHash)
 * 2. InternalUserResponse — internal API (includes passwordHash for auth)
 *
 * Role set is flattened to List<String> in both cases.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    InternalUserResponse toInternalUserResponse(User user);

    List<UserResponse> toUserResponseList(List<User> users);

    @Named("rolesToStrings")
    default List<String> rolesToStrings(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
                .map(Role::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
