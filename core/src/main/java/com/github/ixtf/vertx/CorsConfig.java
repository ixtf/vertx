package com.github.ixtf.vertx;

import lombok.Data;

import java.util.Set;

@Data
public class CorsConfig {
    private Set<String> domainPatterns;
    private Set<String> allowedHeaders;
    private boolean allowCredentials;
}
