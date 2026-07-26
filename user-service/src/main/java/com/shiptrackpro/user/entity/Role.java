package com.shiptrackpro.user.entity;

import com.shiptrackpro.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity — defines authorization levels in the system.
 * Only two roles exist: ADMIN and CUSTOMER.
 * New roles require ADMIN approval (enforced at the application level).
 */
@Entity
@Table(name = "roles", schema = "shiptrack_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
