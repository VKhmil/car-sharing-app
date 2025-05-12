package com.carsharingapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Getter
@Setter
@SQLDelete(sql = "UPDATE roles SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "roles")
public class Role implements GrantedAuthority {
    private static final String PREFIX = "ROLE_";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private RoleName name;

    @Column(name = "is_deleted", unique = true, columnDefinition = "TINYINT(1)")
    private boolean isDeleted;

    @Override
    public String getAuthority() {
        return PREFIX + name;
    }

    public enum RoleName {
        MANAGER,
        CUSTOMER
    }
}
