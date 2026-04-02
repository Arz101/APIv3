package com.spring.api.API.models;

import java.time.OffsetDateTime;
import java.util.*;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(
    name = "comments",
    indexes = {
        @Index(name = "ix_datecreated", columnList = "datecreated")
    }
)
public class Comments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Posts post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "content")
    private String content;

    @ColumnDefault("now()")
    @Column(name = "datecreated")
    private OffsetDateTime dateCreated = OffsetDateTime.now();

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true)
    private Comments parent;

    protected Comments(){}

    public Comments(Posts post, User user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
    }

    public Comments(Posts post, User user, String content, Comments parent) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.parent = parent;
    }
}
