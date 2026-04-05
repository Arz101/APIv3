package com.spring.api.API.Repositories;

import com.spring.api.API.models.BlockedUsers.BlockedUsers;
import com.spring.api.API.models.BlockedUsers.BlockedUsersId;
import com.spring.api.API.models.DTOs.User.UsersBlocked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IBlockedUsersRepository extends JpaRepository<BlockedUsers, BlockedUsersId> {
    @Query("SELECT bu FROM BlockedUsers bu WHERE bu.blocked.id=:blockedId AND bu.user.id=:userId")
    Optional<BlockedUsers> getBlockedUserByBlockedIdAndUserId(@Param("blockedId") Long blockedId, @Param("userId") Long userId);

    @Query("""
        SELECT new com.spring.api.API.models.DTOs.User.UsersBlocked(
            bu.blocked.id,
            bu.blocked.username,
            bu.blockedDate        
        ) 
        FROM BlockedUsers bu 
        WHERE bu.user.id =:userId
        """)
    List<UsersBlocked> getBlockedUsersUsernames(@Param("userId") Long userId);
}
