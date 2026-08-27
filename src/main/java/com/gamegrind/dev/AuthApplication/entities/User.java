package com.gamegrind.dev.AuthApplication.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//@Entity(name = "users") // if dont specify the table name, it will be same as the class name but in lowercase and plural form, so it will be "user" but we want to specify it as "users"

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    UUID Id;

    @Email
    @Column(name = "user_email" , unique = true, nullable = false , length = 300)
    String email;

    @Column(name = "user_name", length = 500)
    String name;

    @Column(name = "password" , nullable = true)
    String password; // we will store the hashed password here, we will use bcrypt to hash the password before storing it in the database


    // url for image directly
    @Column(nullable = true)
    String image;

    @Column(nullable = true)
    private boolean enable = true;

    @Column(nullable = true)
    private Instant createdAt = Instant.now();

    @Column(nullable = true)
    private Instant updatedAt = Instant.now();

//    private String gender;
//    private Address address;

    // as this is user can login from multiple providers like google, facebook, etc. we can have a provider field to store the provider name
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL;
    private String providerId; // this is the id of the user in the provider's system, we can use this to fetch the user from the provider's system if needed


    // when ever we fetch user , it will be fetched together
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable( // created a mini table to store the user and role relationship, as one user can have multiple roles and one role can be assigned to multiple users
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist // runs when the entity is being persisted for the first time, we can use this to set the createdAt field to the current time
    private void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate // runs when the entity is being updated, we can use this to set the updatedAt field to the current time
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return this.email; // we are using email as the username for authentication
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return this.enable;
    }
}
