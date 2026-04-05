package com.spring.api.API.Repositories;

import com.spring.api.API.models.PostsSaved.PostsSaved;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IPostsSavedRepository extends JpaRepository<PostsSaved, Long> {
    @Query("SELECT ps FROM PostsSaved ps WHERE ps.user.id =:userId AND ps.post.id =:postId")
    Optional<PostsSaved> postsSavedByUserIdAndPostsId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("SELECT ps.post.id FROM PostsSaved ps WHERE ps.user.id =:userId")
    List<Long> postsSavedIds(@Param("userId") Long userId);
}
