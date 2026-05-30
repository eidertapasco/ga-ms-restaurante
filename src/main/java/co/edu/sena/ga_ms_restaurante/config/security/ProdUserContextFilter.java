package co.edu.sena.ga_ms_restaurante.config.security;

import co.edu.sena.ga_ms_restaurante.security.UserContext;
import co.edu.sena.ga_ms_restaurante.security.UserContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Profile("prod")
@Order(1)
public class ProdUserContextFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID   = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    private static final String DEFAULT_USER_ID   = "00000000-0000-0000-0000-000000000001";
    private static final String DEFAULT_USER_ROLE = "MESERO";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String rawId   = request.getHeader(HEADER_USER_ID);
            String rawRole = request.getHeader(HEADER_USER_ROLE);

            UUID userId;
            try {
                userId = (rawId != null && !rawId.isBlank())
                        ? UUID.fromString(rawId)
                        : UUID.fromString(DEFAULT_USER_ID);
            } catch (IllegalArgumentException e) {
                log.warn("X-User-Id inválido: '{}', usando default", rawId);
                userId = UUID.fromString(DEFAULT_USER_ID);
            }

            String role = (rawRole != null && !rawRole.isBlank())
                    ? rawRole.toUpperCase()
                    : DEFAULT_USER_ROLE;

            UserContextHolder.setContext(new UserContext(userId, role));
            log.debug("ProdUserContextFilter — userId: {}, role: {}", userId, role);

            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }
}
