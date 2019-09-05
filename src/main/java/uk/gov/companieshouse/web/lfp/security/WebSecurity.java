package uk.gov.companieshouse.web.lfp.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import uk.gov.companieshouse.auth.filter.UserAuthFilter;
import uk.gov.companieshouse.session.handler.SessionHandler;
import uk.gov.companieshouse.auth.filter.HijackFilter;

@EnableWebSecurity
public class WebSecurity {


    @Configuration
    @Order(1)
    public static class TemporaryStartPageSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .antMatcher("/late-filing-penalty");
        }
    }

    @Configuration
    @Order(2)
    public static class LFPWebSecurityFilterConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.antMatcher("/late-filing-penalty/**")
                    .addFilterBefore(new SessionHandler(), BasicAuthenticationFilter.class)
                    .addFilterBefore(new HijackFilter(), BasicAuthenticationFilter.class)
                    .addFilterBefore(new UserAuthFilter(), BasicAuthenticationFilter.class);
        }
    }

}

