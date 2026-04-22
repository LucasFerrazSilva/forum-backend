package com.ferraz.forumbackend.activationtoken;

import com.ferraz.forumbackend.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="tb_activation_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, exclude = {"createdAt", "updatedAt"})
public class ActivationTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ActivationTokenEntity create(UserEntity user) {
        ActivationTokenEntity activationTokenEntity = new ActivationTokenEntity();
        activationTokenEntity.setUser(user);
        activationTokenEntity.setCreatedAt(LocalDateTime.now());
        activationTokenEntity.setUpdatedAt(LocalDateTime.now());
        return activationTokenEntity;
    }

}
