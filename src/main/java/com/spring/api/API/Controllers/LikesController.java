package com.spring.api.API.Controllers;

import com.spring.api.API.services.LikesService;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LikesController {

    private final LikesService likesService;

    public LikesController(LikesService likesService){
        this.likesService = likesService;
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable long postId,
                                      @AuthenticationPrincipal @NonNull UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(likesService.setLike(postId, user.getUsername()));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> unlikePost(@PathVariable long postId,
                                        @AuthenticationPrincipal @NonNull UserDetails user) {
        this.likesService.unlike(postId, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
