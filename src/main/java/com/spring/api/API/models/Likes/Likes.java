package com.spring.api.API.models.Likes;

import java.time.OffsetDateTime;

import com.spring.api.API.models.Posts;
import com.spring.api.API.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Entity
@Table(name = "likes")
public class Likes {

    @EmbeddedId
    private LikesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "publication_id")
    private Posts post;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime created_at;

    protected Likes() {}

    public Likes(User user, Posts post) {
        var userId = user.getId();
        var postId = post.getId();

        this.user = user;
        this.post = post;

        this.id = new LikesId(userId, postId);
    }
}
