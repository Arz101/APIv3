package com.spring.api.API.models.DTOs.Posts;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface PostProjection {
    Long getId();
    String getDescription();
    String getPicture();
    String getUsername();
    Long getLikes();
    Long getComments();
    Instant getDatecreated();
}
