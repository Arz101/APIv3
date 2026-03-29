package com.spring.api.API.models.DTOs.Comments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentsCreateDTO {
    @NotNull
    Long post_id;
    
    @NotBlank
    String content;
}
