package io.alphalens.auth.autoconfigure;

import com.alphalens.auth.controller.AuthController;
import com.alphalens.auth.controller.AuthExceptionHandler;
import com.alphalens.auth.i18n.ApiMessageResolver;
import com.alphalens.auth.security.BearerTokenAuthenticator;
import com.alphalens.auth.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(HttpSecurity.class)
@EnableConfigurationProperties(AuthProperties.class)
public class AuthAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ApiMessageResolver apiMessageResolver() {
        ReloadableResourceBundleMessageSource auth = new ReloadableResourceBundleMessageSource();
        auth.setBasename("classpath:auth-messages");
        auth.setDefaultEncoding("UTF-8");
        auth.setFallbackToSystemLocale(false);
        return new ApiMessageResolver(auth);
    }

    @Bean
    @ConditionalOnBean(com.alphalens.auth.api.AuthFacade.class)
    @ConditionalOnMissingBean(AuthController.class)
    AuthController authController(
            com.alphalens.auth.api.AuthFacade authFacade,
            ApiMessageResolver messages,
            AuthProperties properties) {
        AuthProperties.Cookie cookie = properties.getCookie();
        return new AuthController(
                authFacade,
                messages,
                cookie.isSecure(),
                cookie.getSameSite(),
                cookie.getDomain(),
                cookie.isCrossSiteDeployment(),
                cookie.getRefreshMaxAgeSeconds(),
                cookie.getAccessMaxAgeSeconds());
    }

    @Bean
    @ConditionalOnBean(com.alphalens.auth.api.AuthFacade.class)
    @ConditionalOnMissingBean(AuthExceptionHandler.class)
    AuthExceptionHandler authExceptionHandler(ApiMessageResolver messages) {
        return new AuthExceptionHandler(messages);
    }

    @Bean
    @ConditionalOnBean(BearerTokenAuthenticator.class)
    @ConditionalOnMissingBean
    JwtAuthenticationFilter jwtAuthenticationFilter(
            BearerTokenAuthenticator authenticator,
            AuthProperties properties) {
        return new JwtAuthenticationFilter(
                authenticator,
                properties.getPublicPathPrefixes(),
                properties.getCookie().getAccessTokenName());
    }

    /**
     * Default chain when the app does not declare its own {@link SecurityFilterChain}.
     * AlphaLens defines one (extra public research APIs) so this bean is skipped there.
     */
    @Bean
    @ConditionalOnBean(BearerTokenAuthenticator.class)
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain authSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            AuthProperties properties,
            ObjectProvider<CorsConfigurationSource> corsConfigurationSource) throws Exception {
        return AuthHttpSecurity.statelessJwt(
                        http,
                        corsConfigurationSource.getIfAvailable(),
                        jwtAuthenticationFilter,
                        properties)
                .build();
    }
}
