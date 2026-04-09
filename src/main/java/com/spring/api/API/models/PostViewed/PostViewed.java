package com.spring.api.API.models.PostViewed;

import java.time.OffsetDateTime;

import com.spring.api.API.models.Posts;
import com.spring.api.API.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(
    name = "postviewed",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"})
)
public class PostViewed {
    @EmbeddedId
    private PostViewedId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ColumnDefault("now()")
    @Column(name = "datecreated")
    private OffsetDateTime dateCreated = OffsetDateTime.now();

    @Column(name = "likes")
    private Boolean likes;

    @Column(name = "comments")
    private Boolean comments;

    @Column(name = "save")
    private Boolean save;

    protected PostViewed(){}

    public PostViewed(User user, Posts post) {
        var userId = user.getId();
        var postId = post.getId();

        this.user = user;
        this.post = post;
        this.id = new PostViewedId(userId, postId);
    }
}
