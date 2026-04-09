package com.spring.api.API.models.Follows;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public record FollowsId(
        Long follower,
        Long followed
) implements Serializable {}

