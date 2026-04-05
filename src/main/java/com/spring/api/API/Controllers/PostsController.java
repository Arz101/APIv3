package com.spring.api.API.Controllers;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import com.spring.api.API.models.DTOs.Posts.UpdatePostDTO;
import com.spring.api.API.services.PostsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostsService service;

    public PostsController(PostsService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody CreatePostDTO posts, @NonNull Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(posts, auth.getName()));
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long id,
                                    @RequestParam("file") MultipartFile file,
                                    @AuthenticationPrincipal UserDetails user) {
        this.service.attachImage(id, file, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> myPosts(@NonNull Authentication auth) {
        return ResponseEntity.ok(service.getMyPosts(auth.getName()));
    }

    @GetMapping("/timeline")
    public ResponseEntity<?> timeline(@NonNull Authentication auth) {
        return ResponseEntity.ok(service.timeLine(auth.getName()));
    }

    @GetMapping("/feed")
    public ResponseEntity<?> feed(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.feed(user));
    }

    @GetMapping
    public ResponseEntity<?> userPosts(@RequestParam("username") String username, @NonNull Authentication auth) {
        return ResponseEntity.ok(service.getUserPosts(username, auth.getName()));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable long postId, @NonNull Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.setLike(postId, auth.getName()));
    }

    @DeleteMapping("/{postId}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable long postId, @NonNull Authentication auth) {
        this.service.unlike(postId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/save")
    public ResponseEntity<?> savePosts(@PathVariable long postId, @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.savePosts(postId, user));
    }

    @DeleteMapping("/{postId}/unsave")
    public ResponseEntity<?> unsavePosts(@PathVariable long postId, @AuthenticationPrincipal UserDetails user) {
        this.service.unsavePosts(postId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saved")
    public ResponseEntity<?> savedPostsList(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.savedPostsList(user));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable long postId, @Valid @RequestBody UpdatePostDTO data, @NonNull Authentication auth) {
        return ResponseEntity.ok(service.updatePost(data, postId, auth.getName()));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable long postId, @NonNull Authentication auth) {
        service.deletePost(postId, auth.getName());
        return ResponseEntity.noContent().build();  // 204
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable long postId, @NonNull Authentication auth) {
        return ResponseEntity.ok(service.findPostById(postId, auth.getName()));
    }

    @GetMapping("/{postId}/hashtags")
    public ResponseEntity<?> getPostHashtags(@PathVariable long postId, @NonNull Authentication auth) {
        return ResponseEntity.ok(service.getPostsWithHashTags(postId, auth.getName()));
    }

    @GetMapping("/hashtags/{hashtag}/popular")
    public ResponseEntity<?> getPopularPostsByHashtag(@PathVariable("hashtag") String hashtag, @NonNull Authentication auth) {
        return ResponseEntity.ok(service.mostPopularPostsByHashtag(hashtag, auth.getName()));
    }

    @GetMapping("/following/popular")
    public ResponseEntity<?> getPopularPostsFromFollowing(@NonNull Authentication auth) {
        return ResponseEntity.ok(service.popularPostsLikedByFolloweds(auth.getName()));
    }
}