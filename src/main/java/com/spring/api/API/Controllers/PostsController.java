package com.spring.api.API.Controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import com.spring.api.API.models.DTOs.Posts.UpdatePostDTO;
import com.spring.api.API.services.PostsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;



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

    @PostMapping("/like/{post_id}")
    public ResponseEntity<?> likePost(@PathVariable Long post_id, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(        this.service.setLike(post_id, auth.getName()));
    }

    @PatchMapping("/{post_id}")
    public ResponseEntity<?> updatePost(@PathVariable Long post_id, @RequestBody() UpdatePostDTO data, Authentication auth){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.service.updatePost(data, post_id));
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<?> deletePost(@PathVariable Long post_id, Authentication auth){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.service.deletePost(post_id, auth.getName()));
    }

    @GetMapping("{post_id}")
    public ResponseEntity<?> findPostByIdEntity(@PathVariable Long post_id, Authentication auth) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(this.service.findPostById(post_id));
    }
    
}
