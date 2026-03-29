package com.spring.api.API.models.DTOs.Comments;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentsResponse {
    private Long id;
    private Long post_id;
    private String username;
    private String content;
    private OffsetDateTime dateCreated;
}
