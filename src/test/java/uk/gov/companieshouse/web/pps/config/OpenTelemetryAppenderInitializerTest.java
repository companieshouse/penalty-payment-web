package uk.gov.companieshouse.web.pps.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetrySdkAutoConfiguration;
import org.springframework.boot.opentelemetry.autoconfigure.logging.OpenTelemetryLoggingAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.support.ResourcePropertySource;

@ExtendWith(MockitoExtension.class)
class OpenTelemetryAppenderInitializerTest {

    @Mock
    private OpenTelemetry openTelemetry;

    @Test
    void afterPropertiesSetInstallsTheOpenTelemetryAppender() {
        var initializer = new OpenTelemetryAppenderInitializer(openTelemetry);

        try (MockedStatic<OpenTelemetryAppender> mockedAppender = mockStatic(OpenTelemetryAppender.class)) {
            initializer.afterPropertiesSet();

            mockedAppender.verify(() -> OpenTelemetryAppender.install(openTelemetry));
        }
    }

    @Test
    void beanIsAbsentWhenOpenTelemetryIsNotEnabled() {
        contextRunner().run(context ->
                assertThat(context).doesNotHaveBean(OpenTelemetryAppenderInitializer.class));
    }

    @Test
    void beanIsAbsentWhenOpenTelemetryIsExplicitlyDisabled() {
        contextRunner()
                .withPropertyValues("management.opentelemetry.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(OpenTelemetryAppenderInitializer.class));
    }

    @Test
    void beanIsPresentWhenOpenTelemetryIsEnabled() {
        contextRunner()
                .withPropertyValues("management.opentelemetry.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(OpenTelemetryAppenderInitializer.class));
    }

    /**
     * Loads the real {@code src/main/resources/application.properties} (not a shadowed
     * test-resources copy) with OTel enabled, proving the {@code management.opentelemetry.*}
     * properties actually defined for this service resolve correctly and the context starts.
     */
    @Test
    void realApplicationPropertiesLoadSuccessfullyWithOpenTelemetryEnabled() {
        contextRunner()
                .withConfiguration(AutoConfigurations.of(OpenTelemetryLoggingAutoConfiguration.class))
                .withInitializer(context -> {
                    try {
                        context.getEnvironment().getPropertySources()
                                .addLast(new ResourcePropertySource("classpath:application.properties"));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .withPropertyValues(
                        "management.opentelemetry.enabled=true",
                        "OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(OpenTelemetryAppenderInitializer.class);
                });
    }

    private ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OpenTelemetrySdkAutoConfiguration.class))
                .withUserConfiguration(OpenTelemetryAppenderInitializer.class);
    }
}
