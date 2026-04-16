package com.ferraz.forumbackend.session;

import com.ferraz.forumbackend.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_SESSIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String token;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SessionEntity create(UserEntity user, String token, LocalDateTime expiresAt) {
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setUser(user);
        sessionEntity.setToken(token);
        sessionEntity.setExpiresAt(expiresAt);
        sessionEntity.setCreatedAt(LocalDateTime.now());
        sessionEntity.setUpdatedAt(LocalDateTime.now());
        return sessionEntity;
    }
}

