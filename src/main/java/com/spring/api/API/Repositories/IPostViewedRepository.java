package com.spring.api.API.Repositories;

import com.spring.api.API.models.DTOs.Posts.PostViewedDto;
import com.spring.api.API.models.PostViewed.PostViewedId;
import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.api.API.models.PostViewed.PostViewed;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IPostViewedRepository extends JpaRepository<PostViewed, PostViewedId>{
    @Query("""
        SELECT COUNT(*) >= 1
        FROM PostViewed pv
        WHERE pv.user.id =:user_id AND pv.post.id =:post_id
    """)
    Boolean alreadyViewed(@Param("user_id") Long user_id, @Param("post_id") Long post_id);

    @Query("SELECT pv FROM PostViewed pv WHERE pv.user.id =:userId AND pv.post.id =:postId")
    Optional<PostViewed> postView(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostViewedDto(
            pv.user.username,
            pv.user.id,
            pv.post.id
        )
        FROM PostViewed pv
    """)
    List<PostViewedDto> getAllPostsViewed();
}
