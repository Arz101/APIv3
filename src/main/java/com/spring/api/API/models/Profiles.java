package com.spring.api.API.models;

import com.spring.api.API.models.DTOs.Profile.CreateProfileDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(
        name = "profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "ix_Profiles_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "Profiles_pkey", columnNames = "profile_id")
        }
)
public class Profiles {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "profile_id")
        private Long profileId;

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private User user;

        @Column(name = "name", nullable = false, length = 100)
        private String name;

        @Column(name = "lastname", nullable = false, length = 100)
        private String lastname;

        @Column(name = "birthday", nullable = false)
        private LocalDate birthday;

        @Column(name = "avatar_url")
        private String avatarUrl;

        @Column(name = "gender", length = 20)
        private String gender;

        @Column(name = "phone", length = 15)
        private String phone;

        @Column(name = "bio")
        private String bio;

        @ColumnDefault("false")
        @Column(name = "private", nullable = false)
        private Boolean privateField;

        public Profiles(CreateProfileDTO profileDTO){
                this.name = profileDTO.name();
                this.lastname = profileDTO.lastname();
                this.birthday = profileDTO.birthday();
                this.avatarUrl = profileDTO.avatarUrl();
                this.privateField = profileDTO.privateField();
        }

        public Profiles() {
        }
}
