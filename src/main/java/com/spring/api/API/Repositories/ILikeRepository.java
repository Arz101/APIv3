package com.spring.api.API.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.spring.api.API.models.Likes;

public interface ILikeRepository extends JpaRepository<Likes, Long> {
    @Query("""
        SELECT COUNT(*) 
        FROM Likes l 
        WHERE l.post.id = :postId
    """)
    Long countLikesByPostId(long postId);    
}
