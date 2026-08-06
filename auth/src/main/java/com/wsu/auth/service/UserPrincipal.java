package com.wsu.auth.service;

import com.wsu.core.model.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String userName;
    private final String passwordHash;
    private final String roleName;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    // used at login time — full DB-backed user, real password hash available
    public UserPrincipal(User user) {
        this.userId = user.getId();
        this.userName = user.getUserName();
        this.passwordHash = user.getPassword();
        this.roleName = user.getRole().getName();
        this.active = user.isActive();
        this.authorities = user.getRole().getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .toList();
    }

    // used per-request in JwtAuthFilter — built purely from JWT claims, no DB hit.
    // authorities passed in directly since JwtAuthFilter already extracts them from the token.
    public UserPrincipal(Long userId, String userName, String roleName,
                         Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.userName = userName;
        this.passwordHash = null;   // never needed post-login — token is already proof of authentication
        this.roleName = roleName;
        this.active = true;         // deactivation is enforced at login/refresh time (DB-backed), not per-request
        this.authorities = authorities;
    }

    public static UserPrincipal fromJwtClaims(Long userId, String userName, String roleName,
                                              Collection<? extends GrantedAuthority> authorities) {
        return new UserPrincipal(userId, userName, roleName, authorities);
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return active; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}