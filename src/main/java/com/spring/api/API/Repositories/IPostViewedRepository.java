package com.spring.api.API.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.api.API.models.PostViewed;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IPostViewedRepository extends JpaRepository<PostViewed, Long>{
    @Query("""
        SELECT COUNT(*) >= 1
        FROM PostViewed pv
        WHERE pv.user.id =:user_id AND pv.post.id =:post_id
    """)
    Boolean alreadyViewed(@Param("user_id") Long user_id, @Param("post_id") Long post_id);

    @Query("SELECT pv FROM PostViewed pv WHERE pv.user.id =:userId AND pv.post.id =:postId")
    PostViewed postView(@Param("userId") Long userId, @Param("postId") Long postId);
}
