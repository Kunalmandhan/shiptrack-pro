package com.shiptrackpro.user.entity;

import com.shiptrackpro.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * User entity — single source of truth for all user data in the system.
 *
 * Auth Service delegates user storage here via internal REST calls.
 * The passwordHash is stored but NEVER returned through public APIs.
 *
 * Auth provider tracks whether the user registered via LOCAL (email/password)
 * or GOOGLE (OAuth2), enabling different login flows.
 */
@Entity
@Table(name = "users", schema = "shiptrack_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private String authProvider = "LOCAL";

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    /**
     * Many-to-Many with Role through user_roles join table.
     * EAGER fetch because roles are almost always needed (for JWT claims).
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            schema = "shiptrack_user",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
