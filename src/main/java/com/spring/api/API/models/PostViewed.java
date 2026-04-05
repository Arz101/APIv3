package com.spring.api.API.models;

import java.time.OffsetDateTime;

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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY)
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

    public PostViewed(Posts post, User user) {
        this.post = post;
        this.user = user;
    }
    protected PostViewed(){}
}
