package com.spring.api.API.Controllers;

import com.spring.api.API.services.PostSavedService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController("saved")
public class PostSavedController {

    private final PostSavedService service;

    public PostSavedController(PostSavedService service){
        this.service = service;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<?> savePosts(@PathVariable long postId,
                                       @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.savePosts(postId, user));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> unsavePosts(@PathVariable long postId,
                                         @AuthenticationPrincipal UserDetails user) {
        this.service.unsavePosts(postId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saved")
    public ResponseEntity<?> savedPostsList(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.savedPostsList(user));
    }
}
