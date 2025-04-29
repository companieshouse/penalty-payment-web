package uk.gov.companieshouse.web.pps.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.gov.companieshouse.auth.filter.HijackFilter;
import uk.gov.companieshouse.auth.filter.UserAuthFilter;

import static uk.gov.companieshouse.csrf.config.ChsCsrfMitigationHttpSecurityBuilder.configureApiCsrfMitigations;
import static uk.gov.companieshouse.csrf.config.ChsCsrfMitigationHttpSecurityBuilder.configureWebCsrfMitigations;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurity {

    @Bean
    @Order(1)
    public SecurityFilterChain legacyStartPageSecurityFilterChain(final HttpSecurity http) throws Exception {
        return configureWebCsrfMitigations(
                http.securityMatcher("/late-filing-penalty")
        ).build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain temporaryStartPageSecurityFilterChain(final HttpSecurity http) throws Exception {
        return configureWebCsrfMitigations(
                http.securityMatcher("/pay-penalty")
        ).build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain penaltyRefStartsWithPageSecurityFilterChain(final HttpSecurity http) throws Exception {
        return configureWebCsrfMitigations(
                http.securityMatcher("/pay-penalty/ref-starts-with")
        ).build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain healthcheckSecurityFilterChain(final HttpSecurity http) throws Exception {
        return configureApiCsrfMitigations(
                http.securityMatcher("/pay-penalty/healthcheck")
        ).build();
    }

    @Bean
    @Order(5)
    public SecurityFilterChain scheduledServiceDownSecurityFilterChain(final HttpSecurity http) throws Exception {
        return configureWebCsrfMitigations(
                http.securityMatcher("/pay-penalty/unscheduled-service-down")
        ).build();
    }

    @Bean
    @Order(6)
    public SecurityFilterChain ppsWebSecurityFilterConfig(HttpSecurity http) throws Exception {
        return configureWebCsrfMitigations(
                http.securityMatcher("/pay-penalty/**")
                        .addFilterBefore(new HijackFilter(), BasicAuthenticationFilter.class)
                        .addFilterBefore(new UserAuthFilter(), BasicAuthenticationFilter.class)
        ).build();
    }

}

