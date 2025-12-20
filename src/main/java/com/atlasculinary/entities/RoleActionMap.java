package com.atlasculinary.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID; // Cần import UUID

@Entity
@Table(name = "role_action_map")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RoleActionMapId.class)
public class RoleActionMap {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "action_id")
    private Long actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("actionId")
    @JoinColumn(name = "action_id")
    private Action action;

    @Column(name = "requires_license", nullable = false)
    private Boolean requiresLicense = true;
}