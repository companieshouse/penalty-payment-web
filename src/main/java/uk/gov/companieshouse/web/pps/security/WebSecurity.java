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
import uk.gov.companieshouse.csrf.config.ChsCsrfMitigationHttpSecurityBuilder;
import uk.gov.companieshouse.session.handler.SessionHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurity {
    @Bean
    @Order(1)
    public SecurityFilterChain temporaryStartPageSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/late-filing-penalty");
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain penaltyRefStartsWithPageSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/late-filing-penalty/ref-starts-with");
        return http.build();
    }

    @Bean
    @Order(3)
    protected SecurityFilterChain accessibilityStatementPageSecurityConfig(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/late-filing-penalty/accessibility-statement");
        return http.build();
    }

    @Bean
    @Order(4)
    protected SecurityFilterChain healthcheckSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/late-filing-penalty/healthcheck");
        return http.build();
    }

    @Bean
    @Order(5)
    protected SecurityFilterChain bankTransferSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/late-filing-penalty/bank-transfer/**");
        return http.build();
    }

    @Bean
    @Order(6)
    public SecurityFilterChain lFPWebSecurityFilterConfig (HttpSecurity http) throws Exception {
        http
                .securityMatcher("/late-filing-penalty/**")
                .addFilterBefore(new SessionHandler(), BasicAuthenticationFilter.class)
                .addFilterBefore(new HijackFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(new UserAuthFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }

}

