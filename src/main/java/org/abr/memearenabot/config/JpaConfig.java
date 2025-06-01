package org.abr.memearenabot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration that ensures the DatabaseInitializer runs first
 */
@Configuration
@EnableJpaRepositories(basePackages = "org.abr.memearenabot.repository")
@DependsOn("databaseInitializer")
public class JpaConfig {
    
    @Autowired
    private DatabaseInitializer databaseInitializer;
    
    // No additional configuration needed, the @DependsOn annotation ensures
    // that the DatabaseInitializer bean is created and run before any JPA beans
} 