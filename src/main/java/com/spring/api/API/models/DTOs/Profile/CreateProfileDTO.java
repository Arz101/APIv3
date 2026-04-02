package com.spring.api.API.models.DTOs.Profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CreateProfileDTO (
    @NotNull Long user_id,
    @NotBlank String name,
    @NotBlank String lastname,
    @Past LocalDate birthday,
    String avatar_url,
    Boolean privateField
){
    public CreateProfileDTO {
        if (avatar_url == null) avatar_url = "default_avatar.png";
        if (privateField == null) privateField = false;
    }
}
