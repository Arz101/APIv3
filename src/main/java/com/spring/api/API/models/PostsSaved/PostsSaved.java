package com.spring.api.API.models.PostsSaved;

import com.spring.api.API.models.Posts;
import com.spring.api.API.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.time.OffsetDateTime;

@Setter
@Getter
@Entity
@Table(
        name = "posts_saved",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_post", columnNames = {"user_id", "post_id"})
        }
)
public class PostsSaved {
    @EmbeddedId
    private PostsSavedId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Posts post;

    @ColumnDefault("NOW()")
    @Column(name = "saved_date", nullable = false)
    private OffsetDateTime savedDate = OffsetDateTime.now();

    protected PostsSaved(){}
    public PostsSaved(Long userId, Long postId){
        this.id = new PostsSavedId(userId, postId);
    }
}
