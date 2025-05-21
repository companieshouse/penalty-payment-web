package uk.gov.companieshouse.web.pps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.web.pps.interceptor.LoggingInterceptor;
import uk.gov.companieshouse.web.pps.interceptor.UserDetailsInterceptor;

@SpringBootApplication
public class PPSWebApplication implements WebMvcConfigurer {

    public static final String APPLICATION_NAME_SPACE = "penalty-payment-web";

    private final UserDetailsInterceptor userDetailsInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    public PPSWebApplication(UserDetailsInterceptor userDetailsInterceptor,
                                LoggingInterceptor loggingInterceptor) {
        this.userDetailsInterceptor = userDetailsInterceptor;
        this.loggingInterceptor = loggingInterceptor;
    }

    public static void main(String[] args) {
        SpringApplication.run(PPSWebApplication.class, args);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor);
        registry.addInterceptor(userDetailsInterceptor)
                .excludePathPatterns(
                        "/late-filing-penalty",
                        "/pay-penalty",
                        "/error",
                        "/pay-penalty/ref-starts-with",
                        "/pay-penalty/page-not-found",
                        "/pay-penalty/unscheduled-service-down"
                );
    }
}
