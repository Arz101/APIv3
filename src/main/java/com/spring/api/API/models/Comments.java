package com.spring.api.API.models;

import java.time.OffsetDateTime;

import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Posts post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "content")
    private String content;

    @ColumnDefault("now()")
    @Column(name = "datecreated")
    private OffsetDateTime dateCreated = OffsetDateTime.now();

    public Comments(Posts post, User user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
    }

    protected Comments(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Posts getPost() {
        return post;
    }

    public void setPost(Posts post) {
        this.post = post;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getDatecreated() {
        return dateCreated;
    }

    public void setDatecreated(OffsetDateTime datecreated) {
        this.dateCreated = datecreated;
    }
}
