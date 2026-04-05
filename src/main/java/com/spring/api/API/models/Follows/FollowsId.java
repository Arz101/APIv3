package com.spring.api.API.models.Follows;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class FollowsId implements Serializable {

    private Long followerId;
    private Long followedId;

    protected FollowsId() {}

    public FollowsId(Long followerId, Long followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FollowsId followsId = (FollowsId) o;
        return Objects.equals(followerId, followsId.followerId) && Objects.equals(followedId, followsId.followedId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(followerId, followedId);
    }
}
