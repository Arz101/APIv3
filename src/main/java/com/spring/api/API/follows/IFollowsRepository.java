package com.spring.api.API.follows;

import com.spring.api.API.follows.dtos.FollowSuggestionProjection;
import com.spring.api.API.follows.dtos.LoadGraph;
import com.spring.api.API.follows.dtos.MutualFollowProjection;
import com.spring.api.API.follows.Follows.Follows;
import com.spring.api.API.follows.Follows.FollowsId;
import com.spring.api.API.users.dtos.UserFound;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IFollowsRepository extends JpaRepository<Follows, FollowsId> {
    Optional<Follows> findByFollowerIdAndFollowedId(Long follower_id, Long followed_id);

    @Query("""
        SELECT COUNT(*) >= 1
        FROM Follows f
        WHERE f.followed.id =:followedId AND f.follower.id =:followerId
    """)
    Boolean isFollowOf(@Param("followerId")  Long followerId, @Param("followedId") Long followedId);

    @Query("""
        SELECT f.followed.id
        FROM Follows f 
        WHERE f.follower.username = :followerUsername
    """)
    List<Long> findFollowedUserIdsByFollowerUsername(String followerUsername);

    @Query("""
        SELECT f.follower.username
        FROM Follows f 
        WHERE f.followed.username =:username
    """)
    List<String> findFollowersUsernames(@Param("username") String username);

    @Query("""
        SELECT f.followed.username
        FROM Follows f 
        WHERE f.follower.username =:username
    """)
    List<String> findFollowedUsernames(@Param("username") String username);

    @Query("""
        SELECT new com.spring.api.API.follows.dtos.LoadGraph(
            f.follower.id,
            f.followed.id,
            f.followed.username,
            f.follower.username
        )
        FROM Follows f 
        WHERE f.status = 'active'
    """)
    List<LoadGraph> getAll();

    @Query("""
        SELECT f 
        FROM Follows f
        WHERE f.status = 'pending' AND f.followed.id =:userId AND f.follower.id =:followerId
    """)
    Optional<Follows> getFollowRequest(@Param("userId") Long userId, @Param("followerId") Long followerId);

    @Query("""
        SELECT new com.spring.api.API.users.dtos.UserFound( 
            f.follower.id,
            f.follower.username,
            p.avatarUrl
        )
        FROM Follows f
        INNER JOIN Profiles p ON f.follower.id = p.user.id
        WHERE f.status = 'pending' AND f.followed.id =:userId
    """)
    List<UserFound> findAllFollowRequestByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(*) >= 1
        FROM Follows f
        WHERE f.status = 'active' 
            AND f.followed.id=:targetId 
                AND f.follower.id =:userId
    """)
    Boolean existsFollow(@Param("targetId") Long targetId, @Param("userId") Long userId);

    @Query("""
        SELECT f
        FROM Follows f
        WHERE f.status = 'active' 
            AND f.followed.id=:followingId 
                AND f.follower.id =:followerId
    """)
    Optional<Follows> getFollowByFollowingAndFollowerId(@Param("followingId")Long followingId, @Param("followerId") Long followerId);

    @Query(value = """
        WITH following AS (
            SELECT follower_id AS id FROM follows
            WHERE followed_id = :userId
        ), followers AS (
            SELECT followed_id AS id FROM follows
            WHERE follower_id = :userId
        )
        SELECT u.id as userId
        FROM following fw
        INNER JOIN followers fr
            ON fw.id = fr.id
        INNER JOIN users u
            ON fr.id = u.id
    """, nativeQuery = true)
    List<MutualFollowProjection> mutualFollows(@Param("userId") Long userId);

    @Query(value = """
        WITH following AS (
            SELECT follower_id AS id 
            FROM follows
            WHERE followed_id = :userId AND status = 'active'
        ), followers AS (
            SELECT followed_id AS id 
            FROM follows
            WHERE follower_id = :userId AND status = 'active'
        ), mutual AS (
            SELECT fw.id
            FROM following fw
            INNER JOIN followers fr ON fw.id = fr.id
        )
        SELECT DISTINCT u.username, u.id , COUNT(m.id) AS relevant
        FROM mutual m
        INNER JOIN follows f ON f.follower_id = m.id AND f.status = 'active'
        INNER JOIN users u ON u.id = f.followed_id
        WHERE u.id != :userId AND u.id NOT IN (
            SELECT followed_id 
            FROM follows 
            WHERE follower_id = :userId AND status = 'active'
        )
        GROUP BY u.username, u.id
        ORDER BY relevant DESC
        LIMIT 20
    """, nativeQuery = true)
    List<FollowSuggestionProjection> getFollowsSuggestionByMutualFollows(@Param("userId") Long userId);

}
