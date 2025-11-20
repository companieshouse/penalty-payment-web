package uk.gov.companieshouse.web.pps.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PENALTY_REFERENCE_TYPES_CACHE = "penaltyReferenceTypesCache";

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, MINUTES);
    }

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(PENALTY_REFERENCE_TYPES_CACHE);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

}
