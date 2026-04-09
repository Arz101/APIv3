package com.spring.api.API.Controllers;

import com.spring.api.API.models.DTOs.Comments.CommentReplyCreate;
import com.spring.api.API.models.DTOs.Comments.UpdateCommentDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.spring.api.API.models.DTOs.Comments.CommentsCreateDTO;
import com.spring.api.API.services.CommentsService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("comments")
public class CommentsController {

    private final CommentsService service;
    
    public CommentsController(CommentsService service){
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createComment(@Valid @RequestBody() CommentsCreateDTO comment,
                                           @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(comment, user.getUsername()));
    }

    @PostMapping("/reply/{commentId}")
    public ResponseEntity<?> replayComment(@Valid @RequestBody() CommentReplyCreate comment,
                                           @PathVariable("commentId") Long commentId,
                                           @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.replayComment(comment, user.getUsername(), commentId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable("commentId") Long commentId,
                                           @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.deleteComment(commentId, user.getUsername()));
    }

    @GetMapping("/{postId}/tree")
    public ResponseEntity<?> getCommentsFromPost(@PathVariable("postId") Long postId,
                                                 @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.service.getCommentsByPostId(postId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@Valid @RequestBody()UpdateCommentDTO data,
                                           @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.updateComment(data, user.getUsername()));
    }
}
