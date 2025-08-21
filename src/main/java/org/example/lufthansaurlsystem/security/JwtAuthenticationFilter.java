package org.example.lufthansaurlsystem.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lufthansaurlsystem.security.implementation.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        log.info("Authorization header: '{}'", authHeader);

        String token = null;
        String username = null;

        log.info("Processing request to: {}", request.getRequestURI());
        log.info("Authorization header: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            log.info("Extracted token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");


            if (jwtUtils.validateToken(token)) {
                username = jwtUtils.getUsernameFromToken(token);
                log.info("Token is valid for user: {}", username);
            } else {
                log.warn("Token validation failed");
            }
        } else {
            log.warn("No valid Authorization header found. Got: {}", authHeader);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("User {} authenticated successfully", username);
            } catch (Exception e) {
                log.error("Error loading user details for username: {}", username, e);
            }
            }

        filterChain.doFilter(request, response);
    }
}
