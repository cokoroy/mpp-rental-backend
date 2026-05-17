package com.mpp.rental.config;

import com.mpp.rental.service.CustomUserDetailsService;
import com.mpp.rental.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - Intercepts every HTTP request
 * Extracts JWT token from Authorization header and validates it.
 *
 * UPDATED: Also reads JWT from ?token= query parameter for SSE connections.
 * Standard EventSource (browser) cannot send custom headers, so the frontend
 * passes the token as a query param for the /notifications/stream endpoint.
 *
 * NOTE: request.getParameter() is intentionally skipped for multipart requests
 * (e.g. ToyyibPay callback) because it triggers Tomcat's multipart parser which
 * can throw FileCountLimitExceededException (413) before the request reaches
 * the controller. Multipart endpoints never use JWT query params anyway.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwt = null;

        // 1. Try Authorization header first (standard approach)
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // 2. Fallback: read from ?token= query param (for SSE EventSource connections)
        //    IMPORTANT: skip getParameter() for multipart/form-data requests.
        //    Calling getParameter() on a multipart request forces Tomcat to parse
        //    all parts, which triggers FileCountLimitExceededException (413) if
        //    the number of form fields exceeds Tomcat's maxPartCount (default: 10).
        //    ToyyibPay callback sends 12 multipart fields — this was causing 413.
        if (jwt == null) {
            String contentType = request.getContentType();
            boolean isMultipart = contentType != null
                    && contentType.toLowerCase().contains("multipart/form-data");

            if (!isMultipart) {
                // Safe to call getParameter() — not a multipart request
                String tokenParam = request.getParameter("token");
                if (tokenParam != null && !tokenParam.isEmpty()) {
                    jwt = tokenParam;
                }
            }
            // For multipart requests: no JWT query param support needed
            // (ToyyibPay callback is permitAll() — no token required)
        }

        // 3. No token found — skip JWT processing, let security config decide
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 4. Extract username from token
            final String userEmail = jwtUtil.extractUsername(jwt);

            // 5. Validate token and set authentication if not already set
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Invalid token — don't set authentication, request will fail at authorization
            logger.warn("JWT validation failed: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}