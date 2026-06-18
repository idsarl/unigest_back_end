package gestion.scolaire.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import gestion.scolaire.util.CustomUserDetailService;
import gestion.scolaire.util.JwtUtil;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;

    // @Override   
    // protected void doFilterInternal(HttpServletRequest request,
    //                                 HttpServletResponse response,
    //                                 FilterChain filterChain)
    //         throws ServletException, IOException {

    //     final String authHeader = request.getHeader("Authorization");

    //     String username = null;
    //     String jwt = null;

    //     if (authHeader != null && authHeader.startsWith("Bearer ")) {
    //         jwt = authHeader.substring(7);
    //         username = jwtUtil.extractUsername(jwt);
    //         System.out.println("username : " + username);
    //     }

    //     if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    //         UserDetails userDetails = customUserDetailService.loadUserByUsername(username);

    //         if (jwtUtil.validateToken(jwt, userDetails)) {
    //             UsernamePasswordAuthenticationToken authenticationToken =
    //                     new UsernamePasswordAuthenticationToken(
    //                             userDetails,
    //                             null,
    //                             userDetails.getAuthorities()
    //                     );

    //             authenticationToken.setDetails(
    //                     new WebAuthenticationDetailsSource().buildDetails(request)
    //             );

    //             SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    //         }
    //     }

    //     filterChain.doFilter(request, response);
    // }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                System.out.println("Erreur de validation JWT : " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = customUserDetailService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception e) {
                System.out.println("Authentification JWT ignorée : " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}

