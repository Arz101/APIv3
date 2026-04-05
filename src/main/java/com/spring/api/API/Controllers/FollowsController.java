package com.spring.api.API.Controllers;

import com.spring.api.API.services.FollowService;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("follows")
public class FollowsController {

    private final FollowService service;
    public FollowsController(FollowService service){
        this.service = service;
    }

    @PostMapping("/")
    public ResponseEntity<?> follow_user(@RequestParam("username") String username, @NonNull Authentication auth){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.follow_user(username, auth.getName()));
    }

    @GetMapping("/")
    public ResponseEntity<?> getFollowersUsernames(@NonNull Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.getFollowerUsernames(auth.getName()));
    }

    @PostMapping("/accept/{userId}")
    public ResponseEntity<?> acceptTrackingRequest(@PathVariable("userId") Long userId, @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.acceptRequest(userId, user));
    }

    @GetMapping("/followings")
    public ResponseEntity<?> getFollowingsUsernames(@NonNull Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.getFollowingsUsernames(auth.getName()));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<?> suggestions(@NonNull @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.suggestionFollows(user));
    }

    @DeleteMapping("/unfollow/{userId}")
    public ResponseEntity<?> unfollowUser(@PathVariable("userId") Long userId, @NonNull Authentication auth){
        this.service.unfollow(userId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/decline")
    public ResponseEntity<?> declineFollowRequest(@Param("userId") Long userId, @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.declineFollowRequest(userId, user));
    }

    @GetMapping("/mutual")
    public ResponseEntity<?> mutualFollows(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.mutualFollows(user));
    }
}
