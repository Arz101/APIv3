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
@RequestMapping("posts")
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
    public ResponseEntity<?> myPosts(@AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(service.getMyPosts(user.getUsername()));
    }

    @GetMapping("/timeline")
    public ResponseEntity<?> timeline(@AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(service.timeLine(user.getUsername()));
    }

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(this.service.feed(user, page, size));
    }

    @GetMapping
    public ResponseEntity<?> userPosts(@RequestParam("username") String username,
                                       @AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(service.getUserPosts(username, user.getUsername()));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<?> updatePost(@PathVariable long postId,
                                        @Valid @RequestBody UpdatePostDTO data,
                                        @AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(service.updatePost(data, postId, user.getUsername()));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable long postId,
                                        @AuthenticationPrincipal @NonNull UserDetails user) {
        service.deletePost(postId, user.getUsername());
        return ResponseEntity.noContent().build();  // 204
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable long postId,
                                         @AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(service.findPostById(postId, user.getUsername()));
    }

    @GetMapping("/{postId}/hashtags")
    public ResponseEntity<?> getPostHashtags(@PathVariable long postId,
                                             @AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(service.getPostsWithHashTags(postId, user.getUsername()));
    }

    @GetMapping("/hashtags/{hashtag}/popular")
    public ResponseEntity<?> getPopularPostsByHashtag(@PathVariable("hashtag") String hashtag,
                                                      @AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(service.mostPopularPostsByHashtag(hashtag, user.getUsername()));
    }

    @GetMapping("/following/popular")
    public ResponseEntity<?> getPopularPostsFromFollowing(@AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(service.popularPostsLikedByFolloweds(user.getUsername()));
    }

    @GetMapping("/test/ranking")
    public ResponseEntity<?> testsRanking(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(this.service.testRanking(user));
    }

}