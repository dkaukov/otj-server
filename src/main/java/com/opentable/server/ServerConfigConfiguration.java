package com.opentable.server;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertyResolver;

import com.opentable.service.AppInfo;
import com.opentable.service.EnvInfo;
import com.opentable.spring.ConversionServiceConfiguration;
import com.opentable.spring.PropertySourceUtil;
import com.opentable.spring.SpecializedConfigFactory;

/**
 * Sets up sever configuration management
 */
@Configuration
@Import({
    ConversionServiceConfiguration.class,
    AppInfo.class,
    EnvInfo.class,
})
public class ServerConfigConfiguration {
    private static final String INDENT = "    ";

    /**
     * Create a new {@link PropertySourcesPlaceholderConfigurer}
     * @return a new {@link PropertySourcesPlaceholderConfigurer}
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfig() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Log out all environment key/value pairs at the info level
     * @param env the environment to log
     */
    @Inject
    public void logAppConfig(final ConfigurableEnvironment env) {
        final Logger log = LoggerFactory.getLogger(ServerConfigConfiguration.class);
        log.info("{}\n{}{}", env, INDENT,
                PropertySourceUtil.getKeys(env)
                          .sorted()
                          .map(k -> k + "=" + env.getProperty(k))
                          .collect(Collectors.joining("\n" + INDENT)));
    }

    /**
     * Create a factory that creates {@link ServerConnectorConfig} instances given a name of a connector
     * and the properties for the config are created based on properties named with the pattern "ot.httpserver.connector.<connector name>.<config key name>"
     * @param pr the property resolver to use when looking up config properties
     * @return the server connector config factory
     */
    @Bean
    public SpecializedConfigFactory<ServerConnectorConfig> connectorConfigs(PropertyResolver pr) {
        return SpecializedConfigFactory.create(pr, ServerConnectorConfig.class, "ot.httpserver.connector.${name}");
    }
}
