package com.spring.api.API.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.api.API.models.DTOs.Comments.CommentsCreateDTO;
import com.spring.api.API.services.CommentsService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("comments")
public class CommentsController {

    private CommentsService service;
    
    public CommentsController(CommentsService service){
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createComment(@Valid @RequestBody() CommentsCreateDTO comment, Authentication auth){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.service.create(comment, auth.getName()));
    }    

    @DeleteMapping("/{comment_id}")
    public ResponseEntity<?> deleteComment(@PathVariable("comment_id") Long comment_id, Authentication auth){
        return ResponseEntity.status(HttpStatus.OK).body(this.service.deleteComment(comment_id, auth.getName()));
    }

    @PutMapping("path/{id}")
    public String putMethodName(@PathVariable String id, @RequestBody String entity) {
        
        
        return entity;
    }
}
