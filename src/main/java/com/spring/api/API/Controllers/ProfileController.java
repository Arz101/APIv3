package com.spring.api.API.Controllers;

import com.spring.api.API.models.DTOs.Profile.CreateProfileDTO;
import com.spring.api.API.models.DTOs.Profile.ProfileUpdate;
import com.spring.api.API.services.ProfileService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("profiles")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service){
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody CreateProfileDTO profileDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(profileDTO));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@NonNull Authentication authentication){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.myProfile(authentication.getName()));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProfile(@RequestParam() String username, @NonNull Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.searchProfile(username, auth.getName()));
    }

    @PutMapping("/")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody() ProfileUpdate data, Authentication auth){
        return ResponseEntity.ok(this.service.updateProfile(data, auth.getName()));
    }

    @GetMapping("/{username}/posts")
    public ResponseEntity <?> searchProfilePosts(@PathVariable("username") String username, @NonNull Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.searchProfilePosts(username, auth.getName()));
    }

    @GetMapping("/{username}/likes")
    public ResponseEntity <?> searchProfilePostsLiked(@PathVariable("username") String username, @NonNull Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.searchProfilePostsLiked(username, auth.getName()));
    }

    @GetMapping("/{username}/stats")
    public ResponseEntity<?> getProfileStats(@PathVariable("username") String username, @NonNull Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.get_profile_stats(username, auth.getName()));
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("file") MultipartFile file, @NonNull Authentication auth){
        this.service.storeProfileAvatar(file, auth.getName());
        return ResponseEntity.ok().build();
    }
}
