package uk.jsikora.woodworksapi.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.jsikora.woodworksapi.user.BaseUser;
import uk.jsikora.woodworksapi.user.UserService;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        String email;
        AuthProvider provider;
        try {
            email = jwtService.getEmailFromToken(token);
            provider = AuthProvider.fromString(jwtService.getProviderFromToken(token));

        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (email != null && SecurityContextHolder.getContext()
                                                  .getAuthentication() == null) {
            Optional<BaseUser> user = userService.findByEmailAndProvider(email, provider);
            if (user.isPresent() && jwtService.isTokenValid(token, user.get())) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.get(), null, Collections.emptyList());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext()
                                     .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
