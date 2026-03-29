package com.spring.api.API.services;

import com.spring.api.API.Repositories.IFollowsRepository;
import com.spring.api.API.Repositories.IProfileRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.DTOs.Profile.CreateProfileDTO;
import com.spring.api.API.models.DTOs.Profile.ProfileResponseDTO;
import com.spring.api.API.models.Follows;
import com.spring.api.API.models.Profiles;
import com.spring.api.API.models.User;
import com.spring.api.API.security.Exceptions.ProfilePrivateException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ProfileService {

    private final IProfileRepository repository;
    private final IUserRepository userRepository;
    private final IFollowsRepository followsRepository;

    public ProfileService(
            IProfileRepository repository,
            IUserRepository userRepository,
            IFollowsRepository followsRepository
    ){
        this.repository = repository;
        this.userRepository = userRepository;
        this.followsRepository = followsRepository;
    }

    @Transactional
    public ProfileResponseDTO create(CreateProfileDTO profileDTO) {
        Profiles new_profile = new Profiles(profileDTO);

        User user = this.userRepository.findById(profileDTO.getUser_id())
                .orElseThrow();
        new_profile.setUser(user);

        try {
            Profiles create = this.repository.save(new_profile);
            return new ProfileResponseDTO(
                    create.getProfile_id(),
                    create.getName(),
                    create.getLastname(),
                    create.getBirthday(),
                    create.getAvatar_url(),
                    create.getPrivateField()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ProfileResponseDTO my_profile(String username){
        Profiles profile = this.repository.findProfilesByUserUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new ProfileResponseDTO(
                profile.getProfile_id(),
                profile.getName(),
                profile.getLastname(),
                profile.getBirthday(),
                profile.getAvatar_url(),
                profile.getPrivateField()
        );
    }

    @Transactional(readOnly = true)
    public ProfileResponseDTO search_profile(String target, String currentUser){
        Profiles profile = this.repository.findProfilesByUserUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

         User user = this.userRepository.findByUsername(currentUser)
                 .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        Optional<Follows> follow = this.followsRepository.
                findByFollowerIdAndFollowedId(profile.getUser().getId(), user.getId());

        if (profile.getPrivateField() && follow.isEmpty()){
            throw new ProfilePrivateException("Private profile");
        }

        return new ProfileResponseDTO(
                profile.getProfile_id(),
                profile.getName(),
                profile.getLastname(),
                profile.getBirthday(),
                profile.getAvatar_url(),
                profile.getPrivateField()
        );
    }

    @Transactional
    public String follow_user(String target, String currentUser){
        Profiles profile = this.repository.findProfilesByUserUsername(target)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User user = this.userRepository.findByUsername(currentUser)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        String status = "active";

        if (profile.getPrivateField()){
            status = "pending";
        }

        Follows follow = new Follows(
                user,
                profile.getUser(),
                status
        );

        if (this.followsRepository.existsByFollowerIdAndFollowedId(user.getId(), profile.getUser().getId())){
            this.followsRepository.delete(follow);
            return "Unfollow";
        }
        this.followsRepository.save(follow);
        return "Following";
    }
}
