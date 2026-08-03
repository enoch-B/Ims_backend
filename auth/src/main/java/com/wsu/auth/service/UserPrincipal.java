package com.wsu.auth;

import com.wsu.core.model.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long userId;
    private final String institutionalId;
    private final String passwordHash;
    private final String roleName;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.userId = user.getId();
        this.institutionalId = user.getUserName();
        this.passwordHash = user.getPassword();
        this.roleName = user.getRole().getName();
        this.active = user.isActive();
        this.authorities = user.getRole().getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .toList();
    }

    @Override
    public String getUsername() {
        return institutionalId;
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