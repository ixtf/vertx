package com.github.ixtf.vertx;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class CorsConfig {
    private Set<String> domainPatterns;
    private Set<String> allowedHeaders;
    private boolean allowCredentials;
}
