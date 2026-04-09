package com.spring.api.API.models.DTOs.Profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public record ProfileResponseDTO(
        Long profileId,
        String name,
        String lastname,
        LocalDate birthday,
        String avatarUrl,
        String bio,
        Boolean privateField
){}