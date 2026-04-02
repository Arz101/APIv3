package com.spring.api.API.models.DTOs.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record CreateUserDTO (
    @NotBlank String username,
    @Email String email,
    @NotBlank String password,
    String status
) {
    public CreateUserDTO{
        if(status == null) status = "active";
    }
}
