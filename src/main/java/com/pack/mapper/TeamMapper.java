package com.pack.mapper;

import com.pack.dto.request.TeamRequestDto;
import com.pack.dto.response.TeamResponseDto;
import com.pack.entity.Team;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TeamMapper {

    // FIX: was "owner.username" — User has no username field, uses fullName instead.
    @Mapping(target = "ownerId",       source = "owner.id")
    @Mapping(target = "ownerFullName", source = "owner.fullName")
    @Mapping(target = "memberCount",   ignore = true)   // populated in service
    TeamResponseDto toResponseDto(Team team);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "owner",       ignore = true)
    @Mapping(target = "inviteToken", ignore = true)
    @Mapping(target = "iconUrl",     ignore = true)   // managed exclusively via Image API
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    Team toEntity(TeamRequestDto dto);

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "owner",       ignore = true)
    @Mapping(target = "inviteToken", ignore = true)
    @Mapping(target = "iconUrl",     ignore = true)   // managed exclusively via Image API
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    void updateEntityFromDto(TeamRequestDto dto, @MappingTarget Team team);
}