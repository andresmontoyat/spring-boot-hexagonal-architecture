package my.packagename.infrastructure.driven.adapters.valkey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param ttl upper bound on staleness if an eviction is ever lost
 * @param keyPrefix namespaces keys so several services can share one Valkey
 */
@Validated
@ConfigurationProperties("cache.custom")
public record CustomCacheProperties(@NotNull Duration ttl, @NotBlank String keyPrefix) {}
