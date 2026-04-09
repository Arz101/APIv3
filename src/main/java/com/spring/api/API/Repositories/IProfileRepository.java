package com.spring.api.API.Repositories;

import com.spring.api.API.models.DTOs.Profile.ProfileResponseDTO;
import com.spring.api.API.models.DTOs.Profile.ProfileStats;
import com.spring.api.API.models.Profiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IProfileRepository extends JpaRepository<Profiles, Long> {
    Optional<Profiles> findProfilesByUserUsername(String userUsername);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Profile.ProfileResponseDTO(
            p.profileId,
            p.name,
            p.lastname,
            p.birthday,
            p.avatarUrl,
            p.bio,
            p.privateField
        )
        FROM Profiles p
        WHERE p.user.id =:user_id
    """)
    ProfileResponseDTO getProfileResponseByUserId(@Param("user_id") Long user_id);

    @Query("""
        SELECT p.privateField
        FROM Profiles p
        WHERE p.user.id =:user_id
    """)
    Boolean isPrivate(@Param("user_id") Long user_id);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.Profile.ProfileStats(
            (SELECT COUNT(*) FROM Posts p WHERE p.user.id = u.id) AS posts_count,
            (SELECT COUNT(*) FROM Follows f1 WHERE f1.followed.id = u.id) AS followers_count,
            (SELECT COUNT(*) FROM Follows f2 WHERE f2.follower.id = u.id) AS followeds_count
        )
        FROM User u
        WHERE u.id =:user_id
    """)
    ProfileStats getProfileStats(@Param("user_id") Long user_id);

    @Query("SELECT p FROM Profiles p WHERE p.user.username =:username")
    Optional<Profiles> findProfileByUsername(@Param("username") String username);
}
