package com.spring.api.API.Controllers;

import com.spring.api.API.services.LikesService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class LikesController {

    private final LikesService likesService;

    public LikesController(LikesService likesService){
        this.likesService = likesService;
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId,
                                      @AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(likesService.setLike(postId, user.getUsername()));
    }

    @GetMapping("/{postId}/liked")
    public ResponseEntity<?> isLiked(@PathVariable Long postId, @AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.ok(this.likesService.isLiked(postId, user.getUsername()));
    }

    @GetMapping("/{postId}/likes/count")
    public ResponseEntity<?> getLikesCount(@PathVariable Long postId,
                                           @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(this.likesService.likesCount(postId));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId,
                                        @AuthenticationPrincipal @NonNull UserDetails user) {
        this.likesService.unlike(postId, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
