package com.opencv.ama.starter.config;

import com.opencv.ama.core.engine.AmaEngine;
import com.opencv.ama.core.engine.EngineConfig;
import com.opencv.ama.core.engine.ProviderChain;
import com.opencv.ama.core.engine.ReferenceGenerator;
import com.opencv.ama.core.engine.SecureReferenceGenerator;
import com.opencv.ama.core.spi.AmaStore;
import com.opencv.ama.core.spi.AnswerProvider;
import com.opencv.ama.starter.api.AmaAdminController;
import com.opencv.ama.starter.api.AmaExceptionHandler;
import com.opencv.ama.starter.api.AmaPublicController;
import com.opencv.ama.starter.provider.ProviderFactory;
import com.opencv.ama.starter.rate.RateLimiter;
import com.opencv.ama.starter.store.AmaJdbcStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.List;

/**
 * Auto-configuration for the Ask Me Anything engine.
 *
 * <ul>
 *   <li>{@link AmaJdbcStore} is provided unless the host defines its own {@link AmaStore}.</li>
 *   <li>AI providers are created from {@code ama.providers.*} (only enabled ones) and wired
 *       into a {@link ProviderChain} with failover.</li>
 *   <li>Public and admin REST controllers are registered on web apps.</li>
 * </ul>
 *
 * <p>Security is intentionally NOT configured here: the host app is expected to protect
 * {@code ama.admin.base-path} (default {@code /api/ama/admin}).</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(AmaProperties.class)
public class AmaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AmaStore.class)
    public AmaStore amaStore(DataSource dataSource, AmaProperties properties) {
        return new AmaJdbcStore(new JdbcTemplate(dataSource), properties);
    }

    @Bean
    @ConditionalOnMissingBean(EngineConfig.class)
    public EngineConfig engineConfig(AmaProperties properties) {
        return EngineConfig.defaults()
                .withDefaultMode(properties.getDefaultMode())
                .withAiMode(properties.getAiMode())
                .withKnowledgeThreshold(properties.getKnowledgeThreshold())
                .withLimits(properties.getMaxQuestionLength(), properties.getMaxAskerNameLength())
                .withProviderOrder(properties.getProviderOrder());
    }

    @Bean
    @ConditionalOnMissingBean(ProviderChain.class)
    public ProviderChain providerChain(AmaProperties properties) {
        List<AnswerProvider> providers = properties.enabledProviders().stream()
                .map(ProviderFactory::create)
                .toList();
        return new ProviderChain(providers, properties.getProviderOrder());
    }

    @Bean
    @ConditionalOnMissingBean(AmaEngine.class)
    public AmaEngine amaEngine(AmaStore store, ProviderChain chain, EngineConfig config,
                               ObjectProvider<ReferenceGenerator> referenceGenerator) {
        return new AmaEngine(store, chain, config,
                referenceGenerator.getIfAvailable(SecureReferenceGenerator::new));
    }

    @Bean
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter amaRateLimiter(AmaProperties properties) {
        return new RateLimiter(properties.getRateLimit().isEnabled(),
                properties.getRateLimit().getMaxPerHour(), Duration.ofHours(1));
    }

    @Bean
    @ConditionalOnWebApplication
    public AmaPublicController amaPublicController(AmaEngine engine, RateLimiter rateLimiter) {
        return new AmaPublicController(engine, rateLimiter);
    }

    @Bean
    @ConditionalOnWebApplication
    public AmaAdminController amaAdminController(AmaEngine engine) {
        return new AmaAdminController(engine);
    }

    @Bean
    @ConditionalOnWebApplication
    public AmaExceptionHandler amaExceptionHandler() {
        return new AmaExceptionHandler();
    }
}