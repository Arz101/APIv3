package com.spring.api.API.profiles;

import com.spring.api.API.profiles.dtos.ProfileResponseDTO;
import com.spring.api.API.profiles.dtos.ProfileStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IProfileRepository extends JpaRepository<Profiles, Long> {
    Optional<Profiles> findProfilesByUserUsername(String userUsername);

    @Query("""
        SELECT new com.spring.api.API.profiles.dtos.ProfileResponseDTO(
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
        SELECT new com.spring.api.API.profiles.dtos.ProfileStats(
            (SELECT COUNT(*) FROM Posts p WHERE p.user.id = u.id) AS posts,
            (SELECT COUNT(*) FROM Follows f WHERE f.followed.id = u.id AND f.status = 'active') AS followers,
            (SELECT COUNT(*) FROM Follows f WHERE f.follower.id = u.id AND f.status = 'active') AS followings ,
            (SELECT COUNT(*) FROM Follows f WHERE f.followed.id = u.id AND f.status = 'pending') AS request
        ) 
        FROM User u
        WHERE u.id =:user_id
    """)
    ProfileStats getProfileStats(@Param("user_id") Long user_id);

    @Query("SELECT p FROM Profiles p WHERE p.user.username =:username")
    Optional<Profiles> findProfileByUsername(@Param("username") String username);
}
