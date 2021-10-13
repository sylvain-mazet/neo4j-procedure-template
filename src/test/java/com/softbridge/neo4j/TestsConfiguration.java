package com.softbridge.neo4j;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootConfiguration
@PropertySources({
        @PropertySource("classpath:junit-platform.properties"),
        @PropertySource(value = "file:${softbridge.config}", ignoreResourceNotFound = true),
})
public class TestsConfiguration {

    @Bean
    EmbeddedServerLauncher serverLauncher() {
        return new EmbeddedServerLauncher();
    }

}
