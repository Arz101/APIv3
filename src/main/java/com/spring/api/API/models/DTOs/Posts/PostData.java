package com.spring.api.API.models.DTOs.Posts;


import java.time.OffsetDateTime;

public record PostData(
        Long id,
        String description,
        String picture,
        String username,
        Long likes,
        Long comments,
        OffsetDateTime datecreated
) {
    private static final String BASE_URL = "http://localhost:8080/storage/";

    public PostData {
        picture = picture != null ? BASE_URL + picture : null;
    }
}
