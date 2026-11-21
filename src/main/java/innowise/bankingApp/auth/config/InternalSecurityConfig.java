
package innowise.bankingApp.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class InternalSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain internalSecurityFilterChain(
            HttpSecurity http,
            @Value("${services.internal.auth-password}") String authPassword,
            @Value("${services.internal.order-password}") String orderPassword
    ) throws Exception {
        if (authPassword.isBlank() || orderPassword.isBlank() || authPassword.equals(orderPassword)) {
            throw new IllegalArgumentException("Configure distinct service passwords");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(6);
        InMemoryUserDetailsManager users = new InMemoryUserDetailsManager(
                User.withUsername("authentication-service")
                        .password(encoder.encode(authPassword)).roles("SERVICE").build(),
                User.withUsername("order-service")
                        .password(encoder.encode(orderPassword)).roles("SERVICE").build()
        );
        DaoAuthenticationProvider authentication = new DaoAuthenticationProvider(users);
        authentication.setPasswordEncoder(encoder);

        return http
                .securityMatcher("/internal/**")
                .authenticationProvider(authentication)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/internal/users/{id}").hasRole("SERVICE")
                        .anyRequest().denyAll())
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
