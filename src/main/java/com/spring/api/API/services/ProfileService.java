package com.spring.api.API.services;

import com.spring.api.API.Repositories.IPostsRepository;
import com.spring.api.API.Repositories.IProfileRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.DTOs.Posts.PostResponse;
import com.spring.api.API.models.DTOs.Profile.CreateProfileDTO;
import com.spring.api.API.models.DTOs.Profile.ProfileResponseDTO;
import com.spring.api.API.models.DTOs.Profile.ProfileStats;
import com.spring.api.API.models.DTOs.Profile.ProfileUpdate;
import com.spring.api.API.models.Profiles;
import com.spring.api.API.models.User;
import com.spring.api.API.security.Exceptions.ProfilePrivateException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ProfileService {

    private final IProfileRepository repository;
    private final IUserRepository userRepository;
    private final IPostsRepository postsRepository;
    private final StorageService storage;

    public ProfileService(IProfileRepository repository,
                          IUserRepository userRepository,
                          IPostsRepository postsRepository,
                          StorageService storage
    ){
        this.repository = repository;
        this.userRepository = userRepository;
        this.postsRepository = postsRepository;
        this.storage = storage;

    }

    @Transactional
    public ProfileResponseDTO create(CreateProfileDTO profileDTO) {
        Profiles new_profile = new Profiles(profileDTO);

        User user = this.userRepository.getReferenceById(profileDTO.user_id());
        new_profile.setUser(user);

        try {
            Profiles create = this.repository.save(new_profile);
            return new ProfileResponseDTO(
                    create.getProfile_id(),
                    create.getName(),
                    create.getLastname(),
                    create.getBirthday(),
                    create.getAvatar_url(),
                    create.getBio(),
                    create.getPrivateField()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO myProfile(String username){
        Long user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return this.repository.getProfileResponseByUserId(user_id);
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO searchProfile(String target, String currentUser){
        User targetUser = this.userRepository.findByUsername(target)
                .orElseThrow();

        Long user_id = this.userRepository.getIdByUsername(currentUser)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        if (this.repository.isPrivate(targetUser.getId())) {
            boolean isFollowing = targetUser.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(user_id));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        return this.repository.getProfileResponseByUserId(targetUser.getId());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> searchProfilePosts(String target, String username){
        Long targetUser_id = this.userRepository.getIdByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long curr_user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        User targetUser = this.userRepository.getReferenceById(targetUser_id);

        if (this.repository.isPrivate(targetUser.getId())) {
            boolean isFollowing = targetUser.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(curr_user_id));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        return this.postsRepository.findPosts(targetUser.getId());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> searchProfilePostsLiked(String target, String username){
        Long targetUser_id = this.userRepository.getIdByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long curr_user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        User targetUser = this.userRepository.getReferenceById(targetUser_id);

        if (this.repository.isPrivate(targetUser.getId())) {
            boolean isFollowing = targetUser.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(curr_user_id));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        return this.postsRepository.findPostResponseByIdLikedPosts(targetUser.getId());
    }

    @Transactional(readOnly = true)
    public ProfileStats get_profile_stats(String target, String username){
        Long targetUser_id = this.userRepository.getIdByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long curr_user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        User targetUser = this.userRepository.getReferenceById(targetUser_id);

        if (this.repository.isPrivate(targetUser.getId())) {
            boolean isFollowing = targetUser.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(curr_user_id));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }
        return this.repository.getProfileStats(targetUser_id);
    }

    @Transactional
    public ProfileResponseDTO updateProfile(@NonNull ProfileUpdate data, String username){
        var profile = this.repository.findProfileByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        if(data.name() != null)
            profile.setName(data.name());
        if (data.lastname() != null)
            profile.setLastname(data.lastname());
        if (data.birthday() != null)
            profile.setBirthday(data.birthday());
        if (data.phone() != null)
            profile.setPhone(data.phone());
        if (data.bio() != null)
            profile.setBio(data.bio());
        if (data.private_() != null)
            profile.setPrivateField(data.private_());

        profile = this.repository.save(profile);
        return new ProfileResponseDTO(
                profile.getProfile_id(),
                profile.getName(),
                profile.getLastname(),
                profile.getBirthday(),
                profile.getAvatar_url(),
                profile.getBio(),
                profile.getPrivateField()
        );
    }

    @Transactional
    public void storeProfileAvatar(MultipartFile file, String username){
        String filename = this.storage.save(file);

        var profile = this.repository.findProfileByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Not found"));
        profile.setAvatar_url(filename);
        this.repository.save(profile);

    }
}
