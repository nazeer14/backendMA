package com.pack.mapper;

import com.pack.dto.response.TeamMemberResponseDto;
import com.pack.entity.TeamMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TeamMemberMapper {

    // FIX: was "user.username" / "invitedBy.username" — User has no username field.
    @Mapping(target = "id",                source = "id")
    @Mapping(target = "teamId",            source = "team.id")
    @Mapping(target = "teamName",          source = "team.teamName")
    @Mapping(target = "userId",            source = "user.id")
    @Mapping(target = "userFullName",      source = "user.fullName")
    @Mapping(target = "invitedByUserId",   source = "invitedBy.id")
    @Mapping(target = "invitedByFullName", source = "invitedBy.fullName")
    @Mapping(target = "createdAt",         source = "createdAt")
    @Mapping(target = "updatedAt" ,        source = "updatedAt")
    TeamMemberResponseDto toResponseDto(TeamMember member);
}