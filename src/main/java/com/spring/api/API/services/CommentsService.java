package com.spring.api.API.services;

import com.spring.api.API.Repositories.IPostViewedRepository;
import com.spring.api.API.models.DTOs.Comments.*;
import com.spring.api.API.models.PostViewed;
import com.spring.api.API.security.Exceptions.CommentsActionsUnauthorized;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.spring.api.API.Repositories.ICommentsRepository;
import com.spring.api.API.Repositories.IPostsRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.Comments;
import com.spring.api.API.models.Posts;
import com.spring.api.API.models.User;
import com.spring.api.API.security.Exceptions.CommentsNotFoundExceptions;
import com.spring.api.API.security.Exceptions.PostNotFoundException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;

import java.util.List;

@Service
public class CommentsService {
    
    private final ICommentsRepository repository;
    private final IUserRepository userRepository;
    private final IPostsRepository postsRepository;
    private final IPostViewedRepository postViewedRepository;

    public CommentsService(
        ICommentsRepository repository,
        IUserRepository userRepository,
        IPostsRepository postsRepository,
        IPostViewedRepository postViewedRepository
    ){
        this.repository = repository;
        this.userRepository = userRepository;
        this.postsRepository = postsRepository;
        this.postViewedRepository = postViewedRepository;
    }

    @Transactional
    public CommentResponse create(@NonNull CommentsCreateDTO comment, String username){
        User curr = this.userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Something went wrong"));
        
        Posts post = this.postsRepository.findById(comment.post_id())
            .orElseThrow(() -> new PostNotFoundException("Post not found"));
        
        Comments newComment = this.repository.save(new Comments(
            post,
            curr,
            comment.content()
        ));

        if (!post.getUser().getId().equals(curr.getId())) {
            this.postViewedRepository.save(new PostViewed(post, curr));
        }

        return new CommentResponse(
            newComment.getId(),
            newComment.getContent(),
            newComment.getDateCreated(),
            newComment.getUser().getUsername(),
            null,
            newComment.getPost().getId()
        );
    }

    @Transactional
    public String deleteComment(Long comment_id, String username){
        Long user_id = this.userRepository.getIdByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        Comments comment = this.repository.findByUserIdAndCommentId(user_id, comment_id)
            .orElseThrow(() -> new CommentsNotFoundExceptions("Something went wrong!"));
        this.repository.delete(comment);
        return "Successfully";
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findCommentsByPostId(Long post_id){
        return this.repository.findCommentsByPostId(post_id);
    }

    @Transactional
    public CommentResponse updateComment(@NonNull UpdateCommentDTO data, String username){
        Long user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Comments comment = this.repository.findCommentByUserIdAndCommentId(user_id, data.id())
                .orElseThrow(() -> new CommentsActionsUnauthorized("Unauthorized Actions"));

        comment.setContent(data.content());
        Comments newComment = this.repository.save(comment);

        return new CommentResponse(
                newComment.getId(),
                newComment.getContent(),
                newComment.getDateCreated(),
                newComment.getUser().getUsername(),
                null,
                newComment.getPost().getId()
        );
    }

    @Transactional
    public CommentResponse replayComment(@NonNull CommentReplyCreate newComment,
                                          String username,
                                          Long parent_id){

        var currentUser = this.userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        var commentToRep = this.repository.getReferenceById(parent_id);
        var post = this.postsRepository.getReferenceById(commentToRep.getPost().getId());

        var comment = this.repository.save(new Comments(
                post,
                currentUser,
                newComment.content(),
                commentToRep
        ));

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getDateCreated(),
                comment.getUser().getUsername(),
                commentToRep.getId(),
                comment.getPost().getId()
        );
    }

    @Transactional(readOnly = true)
    public List<?> getCommentsByPostId(Long postId){
        var comments = this.repository.getCommentsByPostId(postId);
        return new CommentsHierarchy().buildHierarchy(comments);
    }
}

