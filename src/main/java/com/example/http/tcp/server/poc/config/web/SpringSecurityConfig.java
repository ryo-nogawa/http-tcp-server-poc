package com.example.http.tcp.server.poc.config.web;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.terasoluna.gfw.security.web.logging.UserIdMDCPutFilter;

/**
 * Bean definition to configure SpringSecurity.
 */
@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    /**
     * Configure ignore security pattern.
     *
     * @return Bean of configured {@link WebSecurityCustomizer}
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(antMatcher("/resources/**"));
    }

    /**
     * Configure {@link SecurityFilterChain} bean.
     *
     * @param http Builder class for setting up authentication and authorization
     * @return Bean of configured {@link SecurityFilterChain}
     * @throws Exception Exception that occurs when setting HttpSecurity
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.formLogin(Customizer.withDefaults());
        http.logout(Customizer.withDefaults());
        http.exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler()));
        http.addFilterAfter(userIdMdcPutFilter(), AnonymousAuthenticationFilter.class);
        http.sessionManagement(Customizer.withDefaults());
        // PoCのため、CSRF保護を無効化する
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(authz -> authz.requestMatchers(antMatcher("/**")).permitAll());

        return http.build();
    }

    /**
     * Configure {@link AccessDeniedHandler} bean.
     *
     * @return Bean of configured {@link AccessDeniedHandler}
     */
    @Bean("accessDeniedHandler")
    public AccessDeniedHandler accessDeniedHandler() {
        // Default error handler
        AccessDeniedHandlerImpl defaultErrorHandler = new AccessDeniedHandlerImpl();
        defaultErrorHandler.setErrorPage("/WEB-INF/views/common/error/accessDeniedError.jsp");

        return defaultErrorHandler;
    }

    /**
     * Configure {@link DefaultWebSecurityExpressionHandler} bean.
     *
     * @return Bean of configured {@link DefaultWebSecurityExpressionHandler}
     */
    @Bean("webSecurityExpressionHandler")
    public DefaultWebSecurityExpressionHandler webSecurityExpressionHandler() {
        return new DefaultWebSecurityExpressionHandler();
    }

    /**
     * Configure {@link UserIdMDCPutFilter} bean.
     *
     * @return Bean of configured {@link UserIdMDCPutFilter}
     */
    @Bean("userIdMDCPutFilter")
    public UserIdMDCPutFilter userIdMdcPutFilter() {
        return new UserIdMDCPutFilter();
    }
}
