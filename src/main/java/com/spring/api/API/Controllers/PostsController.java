package com.spring.api.API.Controllers;

import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;
import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import com.spring.api.API.models.DTOs.Posts.UpdatePostDTO;
import com.spring.api.API.services.PostsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsService service;

    public PostsController(PostsService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody CreatePostDTO posts, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(posts, auth.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyPosts(Authentication auth) {
        return ResponseEntity.ok(service.getMyPosts(auth.getName()));
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(Authentication auth) {
        return ResponseEntity.ok(service.feed(auth.getName()));
    }

    @GetMapping
    public ResponseEntity<?> getUserPosts(@RequestParam("username") String username, Authentication auth) {
        return ResponseEntity.ok(service.getUserPosts(username, auth.getName()));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable long postId, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.setLike(postId, auth.getName()));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable long postId, @Valid @RequestBody UpdatePostDTO data, Authentication auth) {
        return ResponseEntity.ok(service.updatePost(data, postId, auth.getName()));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable long postId, Authentication auth) {
        service.deletePost(postId, auth.getName());
        return ResponseEntity.noContent().build();  // 204
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable long postId, Authentication auth) {
        return ResponseEntity.ok(service.findPostById(postId, auth.getName()));
    }

    @GetMapping("/{postId}/hashtags")
    public ResponseEntity<?> getPostHashtags(@PathVariable long postId, Authentication auth) {
        return ResponseEntity.ok(service.getPostsWithHashTags(postId, auth.getName()));
    }

    @GetMapping("/hashtags/{hashtag}/popular")
    public ResponseEntity<?> getPopularPostsByHashtag(@PathVariable String hashtag, Authentication auth) {
        return ResponseEntity.ok(service.mostPopularPostsByHashtag(hashtag, auth.getName()));
    }

    @GetMapping("/following/popular")
    public ResponseEntity<?> getPopularPostsFromFollowing(Authentication auth) {
        return ResponseEntity.ok(service.popularPostsLikedByFolloweds(auth.getName()));
    }
}