package com.spring.api.API.models.DTOs.Profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CreateProfileDTO (
    @NotNull Long userId,
    @NotBlank String name,
    @NotBlank String lastname,
    @Past LocalDate birthday,
    String avatarUrl,
    Boolean privateField
){
    public CreateProfileDTO {
        if (avatarUrl == null) avatarUrl = "";
        if (privateField == null) privateField = false;
    }
}
