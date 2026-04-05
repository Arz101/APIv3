package com.spring.api.API.models.DTOs.Profile;

import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record ProfileUpdate(
        String name,
        String lastname,
        @Past LocalDate birthday,
        String gender,
        String phone,
        String bio,
        Boolean private_
) {}
