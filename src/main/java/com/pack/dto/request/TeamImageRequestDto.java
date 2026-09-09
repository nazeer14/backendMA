package com.pack.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class TeamImageRequestDto {

    @NotNull(message = "Image file is required")
    private MultipartFile image;
}