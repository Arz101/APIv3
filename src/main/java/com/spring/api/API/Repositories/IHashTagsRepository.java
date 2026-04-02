package com.spring.api.API.Repositories;

import com.spring.api.API.models.DTOs.Posts.HashtagsDTO;
import com.spring.api.API.models.DTOs.Posts.HashtagsProjection;
import com.spring.api.API.models.Hashtags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IHashTagsRepository extends JpaRepository<Hashtags, Long> {
    @Query("SELECT h FROM Hashtags h WHERE h.name =:name ")
    Optional<Hashtags> existsHashtag(@Param("name") String name);

    @Query("""
       SELECT h.name
       FROM Hashtags h
       JOIN h.posts p
       WHERE p.id = :post_id
    """)
    Set<String> getHashtagsByPostId(@Param("post_id") Long post_id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.HashtagsDTO(h.name, ph.id)
        FROM Hashtags h
        JOIN h.posts ph
        WHERE ph.user.id =:user_id
    """)
    List<HashtagsDTO> getAllHashtagsByUserId(@Param("user_id") Long user_id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.HashtagsDTO(h.name, ph.id)
        FROM Hashtags h
        JOIN h.posts ph
        WHERE ph.user.id IN :users_id
    """)
    List<HashtagsDTO> getAllHashtagsByFollowingsId(@Param("users_id") List<Long> users_id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.HashtagsDTO(h.name, p.id)
        FROM Posts p
        JOIN p.hashtags h
        WHERE p.id IN :posts_id
    """)
    List<HashtagsDTO> getHashtagsByPostIdList(@Param("posts_id") List<Long> posts_id);

    @Query("""
        SELECT h.id
        FROM Hashtags h
        WHERE h.name =:name
    """)
    Optional<Long> getIdByName(@Param("name") String name);

    @Query(value = """
        SELECT ph.hashtag_id AS hashtags 
        FROM post_hashtag ph
        INNER JOIN (
        	SELECT publication_id AS post_id, created_at
        	FROM likes l
        	WHERE l.user_id =:user_id
        	ORDER BY l.created_at DESC
        	LIMIT 100
        ) x ON x.post_id = ph.post_id
        GROUP BY ph.hashtag_id
        ORDER BY  COUNT(*) DESC
        LIMIT 5
    """, nativeQuery = true)
    HashtagsProjection getPrincipalInterestsBasedOnHashtags(@Param("user_id") Long user_id);
}
