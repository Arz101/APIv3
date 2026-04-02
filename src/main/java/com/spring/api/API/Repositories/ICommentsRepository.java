package com.spring.api.API.Repositories;

import java.util.List;
import java.util.Optional;

import com.spring.api.API.models.DTOs.Comments.CommentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.spring.api.API.models.Comments;


public interface ICommentsRepository extends JpaRepository<Comments, Long>{
    
    @Query("""
        SELECT c
        FROM Comments c
        WHERE c.id =:comment_id AND c.user.id =:user_id
    """)
    Optional<Comments> findByUserIdAndCommentId(@Param("user_id") Long user_id, @Param("comment_id") Long comment_id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Comments.CommentResponse(
            c.id,
            c.content,
            c.dateCreated,
            c.user.username,
            c.parent.id,
            c.post.id
        )
        FROM Comments c
        WHERE c.post.id =:post_id
        ORDER BY c.dateCreated
    """)
    List<CommentResponse> findCommentsByPostId(@Param("post_id") Long post_id);

    @Query("""
        SELECT c
        FROM Comments c
        WHERE c.user.id =:user_id AND c.id=:comment_id
    """)
    Optional<Comments> findCommentByUserIdAndCommentId(@Param("user_id") Long user_id, @Param("comment_id") Long comment_id);

    @Query("""
        SELECT c
        FROM Comments c
        WHERE c.id =:comment_id
    """)
    Optional<Comments> findCommentById(@Param("comment_id") Long comment_id);

    @Query(value = """
        SELECT new com.spring.api.API.models.DTOs.Comments.CommentResponse(
            c.id,
            c.content,
            c.dateCreated,
            c.user.username,
            c.parent.id,
            c.post.id
        )
        FROM Comments c
        WHERE c.post.id =:postId 
    """)
    List<CommentResponse> getCommentsByPostId(@Param("postId") Long postId);
}

