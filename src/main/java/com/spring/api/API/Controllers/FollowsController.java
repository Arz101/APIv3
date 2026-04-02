package com.spring.api.API.Controllers;

import com.spring.api.API.services.FollowService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<?> getFollowersUsernames(Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.getFollowerUsernames(auth.getName()));
    }

    @GetMapping("/followeds")
    public ResponseEntity<?> getFollowedUsernames(Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.getFollowedUsernames(auth.getName()));
    }
}
