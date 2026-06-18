package fa.training.librarymanagementsystem.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless security configuration.
 * CSRF is disabled and sessions are never created; authentication is carried entirely by JWT.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // Books: GET open to authenticated; write ops admin-only
                        .requestMatchers(HttpMethod.GET, "/api/books/**").authenticated()
                        .requestMatchers("/api/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/borrow").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/return").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/renew").authenticated()
                        // Reader can view own history; all other borrow-record endpoints are admin-only
                        .requestMatchers("/api/borrow-records/my").authenticated()
                        .requestMatchers("/api/borrow-records/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/stats/**").hasRole("ADMIN")
                        // Categories: GET open to authenticated; write ops admin-only
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                        .requestMatchers("/api/categories/**").hasRole("ADMIN")
                        // Fines: view-own open to authenticated; pay/waive/list-all admin-only
                        .requestMatchers(HttpMethod.GET, "/api/fines/my-fines").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/fines/my-history").authenticated()
                        .requestMatchers("/api/fines/**").hasRole("ADMIN")
                        // Reservations: create/view-own/cancel open to authenticated; list-all admin-only
                        .requestMatchers(HttpMethod.POST, "/api/reservations").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/my").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reservations/**").authenticated()
                        .requestMatchers("/api/reservations/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // Return ApiResponse-shaped JSON instead of Spring's default redirect on 401
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\",\"data\":null}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}