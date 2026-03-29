package com.spring.api.API.Repositories;

import com.spring.api.API.models.Posts;
import com.spring.api.API.models.User;
import com.spring.api.API.models.DTOs.Posts.PostResponse;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface IPostsRepository extends JpaRepository<Posts, Long> {
    Optional<Posts> findById(long id);

    @Query("""
        SELECT p 
        FROM Posts p 
        WHERE p.profile.user.username = :username
    """)
    List<Posts> findByUsername(String username);

        @Query("""
            SELECT p 
            FROM Posts p 
            WHERE p.profile.user.username IN :followingUsernames
            ORDER BY p.datecreated DESC
        """)
    List<Posts> findByFollowingUsernames(List<String> followingUsernames);


    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostResponse(
            p.id,
            p.description,
            p.picture,
            p.profile.user.username,
            COUNT(l.id),
            p.datecreated
        )
        FROM Posts p
        LEFT JOIN Likes l ON l.post = p
        WHERE p.profile.user.id IN :followingUserIds
        GROUP BY 
            p.id,
            p.description,
            p.picture,
            p.profile.user.username,
            p.datecreated
        ORDER BY p.datecreated DESC
    """)
    List<PostResponse> findFeed(@Param("followingUserIds") List<Long> followingUserIds, Pageable pageable);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostResponse(
            p.id,
            p.description,
            p.picture,
            p.profile.user.username,
            COUNT(l.id),
            p.datecreated
        )
        FROM Posts p
        LEFT JOIN Likes l ON l.post = p
        WHERE p.profile.user =:user  
        GROUP BY 
            p.id,
            p.description,
            p.picture,
            p.profile.user.username,
            p.datecreated
        ORDER BY p.datecreated DESC
    """)
    List<PostResponse> findMePosts(@Param("user") User user);

    @Query("""
        SELECT p
        FROM Posts p
        WHERE p.profile.user =:user AND p.id =:post_id
    """)
    Optional<Posts> findPostByUserAndPostId(@Param("user") User user, Long post_id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostResponse(
            p.id,
            p.description,
            p.picture,
            p.profile.user.username,
            COUNT(l.id),
            p.datecreated
        ) 
        FROM Posts p
        LEFT JOIN Likes l ON l.post = p
        WHERE p.id =:post_id
        GROUP BY 
            p.id,
            p.description,
            p.picture,
            p.profile.user.username,
            p.datecreated
    """)
    Optional<PostResponse> findPostResponseById(@Param("post_id") Long post_id);
}
