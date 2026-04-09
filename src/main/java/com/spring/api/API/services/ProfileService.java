package com.spring.api.API.services;

import com.spring.api.API.Repositories.IFollowsRepository;
import com.spring.api.API.Repositories.IPostsRepository;
import com.spring.api.API.Repositories.IProfileRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.DTOs.Posts.PostData;
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
    private final IFollowsRepository followsRepository;

    public ProfileService(IProfileRepository repository,
                          IUserRepository userRepository,
                          IPostsRepository postsRepository,
                          StorageService storage,
                          IFollowsRepository followsRepository
    ){
        this.repository = repository;
        this.userRepository = userRepository;
        this.postsRepository = postsRepository;
        this.storage = storage;
        this.followsRepository = followsRepository;
    }

    @Transactional
    public ProfileResponseDTO create(CreateProfileDTO profileDTO) {
        Profiles newProfile = new Profiles(profileDTO);

        User user = this.userRepository.getReferenceById(profileDTO.userId());
        newProfile.setUser(user);

        try {
            Profiles create = this.repository.save(newProfile);
            return new ProfileResponseDTO(
                    create.getProfileId(),
                    create.getName(),
                    create.getLastname(),
                    create.getBirthday(),
                    create.getAvatarUrl(),
                    create.getBio(),
                    create.getPrivateField()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO myProfile(String username){
        Long userId = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return this.repository.getProfileResponseByUserId(userId);
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO searchProfile(String target, String currentUser){
        User targetUser = this.userRepository.findByUsername(target)
                .orElseThrow();

        Long userId = this.userRepository.getIdByUsername(currentUser)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        if (this.repository.isPrivate(targetUser.getId())) {
            boolean isFollowing = targetUser.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(userId));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        return this.repository.getProfileResponseByUserId(targetUser.getId());
    }

    @Transactional(readOnly = true)
    public List<PostData> searchProfilePosts(String target, String username){
        Long targetUserId = this.userRepository.getIdByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long currUserId = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        User targetUser = this.userRepository.getReferenceById(targetUserId);

        this.verifyAccess(targetUserId, currUserId);

        return this.postsRepository.findPosts(targetUser.getId());
    }

    @Transactional(readOnly = true)
    public List<PostData> searchProfilePostsLiked(String target, String username){
        Long targetUserId = this.userRepository.getIdByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long currUserId = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        User targetUser = this.userRepository.getReferenceById(targetUserId);

        this.verifyAccess(targetUserId, currUserId);

        return this.postsRepository.findPostResponseByIdLikedPosts(targetUser.getId());
    }

    @Transactional(readOnly = true)
    public ProfileStats get_profile_stats(String target, String username){
        Long targetUserId = this.userRepository.getIdByUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Long currUserId = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        User targetUser = this.userRepository.getReferenceById(targetUserId);

        if (this.repository.isPrivate(targetUser.getId())) {
            boolean isFollowing = targetUser.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(currUserId));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }
        return this.repository.getProfileStats(targetUserId);
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
                profile.getProfileId(),
                profile.getName(),
                profile.getLastname(),
                profile.getBirthday(),
                profile.getAvatarUrl(),
                profile.getBio(),
                profile.getPrivateField()
        );
    }

    @Transactional
    public void storeProfileAvatar(MultipartFile file, String username){
        String filename = this.storage.save(file);

        var profile = this.repository.findProfileByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Not found"));
        profile.setAvatarUrl(filename);
        this.repository.save(profile);

    }

    private void verifyAccess(Long targetId, Long currentId){
        if (this.repository.isPrivate(targetId)) {
            boolean isFollowing = this.followsRepository.isFollowOf(currentId, targetId);
            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }
    }
}
