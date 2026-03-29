package com.spring.api.API.Repositories;

import java.util.Optional;

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
    Optional<Comments> findByUserIdAndCommentId(@Param("user_id") Long user_id, @Param("comment_id") Long commet_id);
}
