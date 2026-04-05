package com.spring.api.API.models.PostsSaved;

import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public record PostsSavedId(
        Long userId,
        Long postId
) implements Serializable {}
