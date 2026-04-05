package com.spring.api.API.models.BlockedUsers;

import com.spring.api.API.models.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "blocked_users"
)
public class BlockedUsers {
    @EmbeddedId
    private BlockedUsersId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("blockedId")
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @ColumnDefault("NOW()")
    @Column(name = "blocked_date", nullable = false)
    private OffsetDateTime blockedDate = OffsetDateTime.now();

    public BlockedUsers(Long userId, Long blockedId){
        this.id = new BlockedUsersId(userId, blockedId);
    }
}
