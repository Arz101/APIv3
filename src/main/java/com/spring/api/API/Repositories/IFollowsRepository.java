package com.spring.api.API.Repositories;

import com.spring.api.API.models.Follows;
import com.spring.api.API.models.FollowsId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IFollowsRepository extends JpaRepository<Follows, FollowsId> {
    Optional<Follows> findByFollowerIdAndFollowedId (Long follower_id, Long followed_id);
    Boolean existsByFollowerIdAndFollowedId (Long follower_id, Long followed_id);

    @Query("""
        SELECT f.followed.id
        FROM Follows f 
        WHERE f.follower.username = :followerUsername
    """)
    List<Long> findFollowedUserIdsByFollowerUsername(String followerUsername);
}
