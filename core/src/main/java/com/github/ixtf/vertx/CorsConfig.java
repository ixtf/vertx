package com.github.ixtf.vertx;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class CorsConfig {
    private final Set<String> domainPatterns;
    private final Set<String> allowedHeaders;
    private final boolean allowCredentials;
}
