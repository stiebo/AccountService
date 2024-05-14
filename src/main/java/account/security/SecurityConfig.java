package account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    public SecurityConfig (RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                           CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.accessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .httpBasic(c -> c.authenticationEntryPoint(restAuthenticationEntryPoint))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .csrf(AbstractHttpConfigurer::disable) // For Postman
                //.headers(headers -> headers.frameOptions().disable()) // For the H2 console
                .authorizeHttpRequests(auth -> auth
                        //opt1: permit dispatches to prevent 401, see also: https://stackoverflow.com/questions/74971183/
                        //.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.FORWARD,
                        // DispatcherType.ERROR).permitAll()
                        // opt2: (endpoints redirecting to the /error/** in case of error and /error/ is secured
                        // by spring security)
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup", "/actuator/shutdown").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ACCOUNTANT", "ROLE_ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment")
                        .hasAnyAuthority("ROLE_USER", "ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments")
                        .hasAuthority("ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments")
                        .hasAuthority("ROLE_ACCOUNTANT")
                        .requestMatchers(HttpMethod.GET, "/api/security/events/")
                        .hasAuthority("ROLE_AUDITOR")
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMINISTRATOR")
                        .anyRequest().denyAll()
                )

                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
