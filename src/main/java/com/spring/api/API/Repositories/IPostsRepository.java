package com.spring.api.API.Repositories;

import com.spring.api.API.models.DTOs.Posts.PostProjection;
import com.spring.api.API.models.Posts;
import com.spring.api.API.models.DTOs.Posts.PostData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IPostsRepository extends JpaRepository<Posts, Long> {
    @Query("SELECT p FROM Posts p WHERE p.id=:id")
    Optional<Posts> findById(Long id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostData(
            p.id,
            p.description,
            p.picture,
            p.user.username,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT c.id),
            p.datecreated
        )
        FROM Posts p
        LEFT JOIN Likes l ON l.post = p
        LEFT JOIN Comments c ON c.post = p
        WHERE p.id IN :postsId
        GROUP BY 
            p.id,
            p.description,
            p.picture,
            p.user.username,
            p.datecreated
    """)
    List<PostData> getPostsResponseByIdList(@Param("postsId") List<Long> postsId);

    @Query("""
        SELECT p 
        FROM Posts p 
        WHERE p.user.username = :username
    """)
    List<Posts> findByUsername(String username);

    @Query("""
        SELECT p 
        FROM Posts p 
        WHERE p.user.username IN :followingUsernames
        ORDER BY p.datecreated DESC
    """)
    List<Posts> findByFollowingUsernames(List<String> followingUsernames);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostData(
            p.id,
            p.description,
            p.picture,
            p.user.username,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT c.id),
            p.datecreated
        )
        FROM Posts p
        LEFT JOIN Likes l ON l.post.id = p.id
        LEFT JOIN Comments c ON c.post.id = p.id
        GROUP BY p.id, p.description, p.picture, p.user.username, p.datecreated
        ORDER BY p.datecreated DESC
    """)
    List<PostData> getAllPosts();


    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostData(
            p.id,
            p.description,
            p.picture,
            p.user.username,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT c.id),
            p.datecreated
        )
        FROM Posts p
        LEFT JOIN Likes l ON l.post = p
        LEFT JOIN Comments c ON c.post = p
        WHERE p.user.id IN :followingUserIds
        GROUP BY 
            p.id,
            p.description,
            p.picture,
            p.user.username,
            p.datecreated
        ORDER BY p.datecreated DESC
    """)
    List<PostData> findFeed(@Param("followingUserIds") List<Long> followingUserIds, Pageable pageable);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostData(
            p.id,
            p.description,
            p.picture,
            p.user.username,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT c.id),
            p.datecreated
        )
        FROM Posts p
        LEFT JOIN Likes l ON l.post = p
        LEFT JOIN Comments c ON c.post = p
        WHERE p.user.id =:user_id  
        GROUP BY 
            p.id,
            p.description,
            p.picture,
            p.user.username,
            p.datecreated
        ORDER BY p.datecreated DESC
    """)
    List<PostData> findPosts(@Param("user_id") Long user_id);

    @Query("""
        SELECT p
        FROM Posts p
        WHERE p.user =:user_id AND p.id =:post_id
    """)
    Optional<Posts> findPostByUserAndPostId(@Param("user_id") Long user_id, Long post_id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostData(
            p.id,
            p.description,
            p.picture,
            p.user.username,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT c.id),
            p.datecreated
        ) 
        FROM Posts p
        LEFT JOIN Likes l ON l.post = p
        LEFT JOIN Comments c ON c.post = p
        WHERE p.id =:post_id
        GROUP BY 
            p.id,
            p.description,
            p.picture,
            p.user.username,
            p.datecreated
    """)
    Optional<PostData> findPostResponseById(@Param("post_id") Long post_id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Posts.PostData(
            p.id,
            p.description,
            p.picture,
            p.user.username,
            COUNT(DISTINCT l.id),
            COUNT(DISTINCT c.id),
            p.datecreated
        ) 
        FROM Posts p
        LEFT JOIN Likes l ON l.post = p
        LEFT JOIN Comments c ON c.post = p
        WHERE l.user.id =:user_id    
        GROUP BY
           p.id,
           p.description,
           p.picture,
           p.user.username,
           p.datecreated,
           l.created_at
   """)
    List<PostData> findPostResponseByIdLikedPosts(@Param("user_id") Long user_id);

    @Query(value = """
        SELECT
            p.id AS id,
            p.description AS description,
            p.picture AS picture,
            p.datecreated AS datecreated,
            u.username AS username,
            COALESCE(l.like_count, 0) AS likes,
            COALESCE(c.comments_count, 0) AS comments,
            p.datecreated AT TIME ZONE 'UTC' AS datecreated        
        FROM posts p
        
        JOIN profiles pf
            ON pf.user_id = p.user_id
        
        JOIN users u
            ON u.id = p.user_id
        
        LEFT JOIN follows f
            ON f.followed_id = p.user_id
            AND f.follower_id = :userId
            AND f.status = 'active'
                
        JOIN post_hashtag ph
            ON ph.post_id = p.id
        
        LEFT JOIN (
            SELECT publication_id, COUNT(*) AS like_count
            FROM likes
            GROUP BY publication_id
        ) l ON l.publication_id = p.id
            
        LEFT JOIN (
            SELECT post_id, COUNT(*) AS comments_count
            FROM comments
            GROUP BY post_id
        ) c ON c.post_id = p.id
        
        WHERE ph.hashtag_id IN :hashtagsIds
        AND (
            pf.private = false
            OR f.follower_id IS NOT NULL
        )
        
        ORDER BY likes DESC
        LIMIT 10
    """, nativeQuery = true)
    List<PostProjection> mostPopularPostsByHashtags(
            @Param("userId") Long userId,
            @Param("hashtagsIds") List<Long> hashtagsIds
    );

    @Query(value = """        
        SELECT DISTINCT
            p.id,
            p.description,
            p.picture,
        	u.username,
        	COALESCE(lc.likes_count, 0) AS likes,
        	COALESCE(c.comments_count,0) AS comments,
            p.datecreated
        FROM posts p
        INNER JOIN likes l
        	ON l.publication_id = p.id
        INNER JOIN (
        	SELECT f.followed_id as followed
        	FROM follows f
        	WHERE f.follower_id =:user_id 
            	AND f.status = 'active'
        ) fl ON fl.followed = l.user_id
        INNER JOIN users u
        	ON u.id = p.user_id
        INNER JOIN (
        	SELECT publication_id AS post_id, COUNT(*) AS likes_count
        	FROM likes
        	GROUP BY publication_id
        ) lc ON lc.post_id = p.id
        LEFT JOIN (
        	SELECT post_id , COUNT(*) AS comments_count
        	FROM comments
        	GROUP BY post_id
        ) c ON c.post_id = p.id
        ORDER BY likes DESC
        LIMIT 10
    """, nativeQuery = true)
    List<PostProjection> mostPopularPostLikedByFollowings(@Param("user_id") Long user_id);
}
