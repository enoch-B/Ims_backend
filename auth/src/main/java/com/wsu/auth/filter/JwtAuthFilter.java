package com.wsu.auth.filter;

import com.wsu.auth.service.UserPrincipal;
import com.wsu.auth.util.JwtUtil;
import com.wsu.core.Exception.ResourceNotFoundException;
import com.wsu.core.model.entities.User;
import com.wsu.core.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository=userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String type = jwtUtil.extractType(token);
            String subject = jwtUtil.extractSubject(token);

            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.isTokenValid(token, subject)) {

                    if ("password_change".equals(type)) {
                        var authorities = List.of(new SimpleGrantedAuthority("PASSWORD_CHANGE_ONLY"));
                        var authToken = new UsernamePasswordAuthenticationToken(subject, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    } else {
                        Long userId = jwtUtil.extractUserId(token);
                        String role = jwtUtil.extractRole(token);
                        List<String> permissions = jwtUtil.extractPermissions(token);

                        var authorities = permissions.stream()
                                .map(SimpleGrantedAuthority::new)
                                .toList();

                        User user= userRepository.findById(userId)
                                .orElseThrow(()-> new ResourceNotFoundException("User Not found"));

                       //UserPrincipal principal = UserPrincipal.fromJwtClaims(userId, subject, role, authorities);
                        UserPrincipal principal= new UserPrincipal(user);

                        var authToken = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            // invalid/expired token -> leave context unauthenticated, let downstream 401
        }

        filterChain.doFilter(request, response);
    }
}