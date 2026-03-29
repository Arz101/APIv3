package com.spring.api.API.models;


import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "posts",
        indexes = {
                @Index(name = "ix_Publications_id", columnList = "id")
        }
)
public class Posts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "profile_id")
    private Profiles profile;

    @NotNull
    @Column(name = "description", nullable = false, length = Integer.MAX_VALUE)
    private String description;

    @Size(max = 255)
    @Column(name = "picture")
    private String picture;

    @ColumnDefault("now()")
    @Column(name = "datecreated")
    private OffsetDateTime datecreated;

    public Posts(){}

    public Posts(CreatePostDTO postsDTO, Profiles profile){
        this.description = postsDTO.getDescription();
        this.picture = postsDTO.getPicture();
        this.profile = profile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profiles getProfile() {
        return profile;
    }

    public void setProfile(Profiles profile) {
        this.profile = profile;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public OffsetDateTime getDatecreated() {
        return datecreated;
    }

    public void setDatecreated(OffsetDateTime datecreated) {
        this.datecreated = datecreated;
    }
}
