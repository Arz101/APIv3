package com.spring.api.API.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import com.spring.api.API.services.PostsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/posts")
public class PostsController {
    
    private final PostsService service;
    public PostsController(PostsService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> postMethodName(@Valid @RequestBody CreatePostDTO posts, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(posts, auth.getName()));
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getMyPosts(Authentication auth) {
        return ResponseEntity.ok(this.service.getPostsByUsername(auth.getName()));
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(Authentication auth) {
        return ResponseEntity.ok(this.service.feed(auth.getName()));    
    }
}
