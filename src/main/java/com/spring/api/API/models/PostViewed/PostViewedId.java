package com.spring.api.API.models.PostViewed;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public record PostViewedId(
        Long userId,
        Long postId
) implements Serializable
{}
