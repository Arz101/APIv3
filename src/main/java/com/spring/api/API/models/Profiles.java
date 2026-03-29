package com.spring.api.API.models;

import com.spring.api.API.models.DTOs.Profile.CreateProfileDTO;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

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
        private Long profile_id;

        @OneToOne()
        @JoinColumn(name = "user_id")
        private User user;

        @Column(name = "name", nullable = false, length = 100)
        private String name;

        @Column(name = "lastname", nullable = false, length = 100)
        private String lastname;

        @Column(name = "birthday", nullable = false)
        private LocalDate birthday;

        @Column(name = "avatar_url")
        private String avatar_url;

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
                this.name = profileDTO.getName();
                this.lastname = profileDTO.getLastname();
                this.birthday = profileDTO.getBirthday();
                this.avatar_url = profileDTO.getAvatar_url();
                this.privateField = profileDTO.getPrivateField();
        }

        public Profiles() {

        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public String getLastname() {
                return lastname;
        }

        public void setLastname(String lastname) {
                this.lastname = lastname;
        }

        public LocalDate getBirthday() {
                return birthday;
        }

        public void setBirthday(LocalDate birthday) {
                this.birthday = birthday;
        }

        public String getAvatar_url() {
                return avatar_url;
        }

        public void setAvatar_url(String avatar_url) {
                this.avatar_url = avatar_url;
        }

        public String getGender() {
                return gender;
        }

        public void setGender(String gender) {
                this.gender = gender;
        }

        public String getPhone() {
                return phone;
        }

        public void setPhone(String phone) {
                this.phone = phone;
        }

        public String getBio() {
                return bio;
        }

        public void setBio(String bio) {
                this.bio = bio;
        }

        public Boolean getPrivateField() {
                return privateField;
        }

        public void setPrivateField(Boolean privateField) {
                this.privateField = privateField;
        }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getProfile_id() {
        return profile_id;
    }

    public void setProfile_id(Long profile_id) {
        this.profile_id = profile_id;
    }
}
