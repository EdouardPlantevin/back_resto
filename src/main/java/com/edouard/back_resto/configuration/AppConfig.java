package com.edouard.back_resto.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Configuration
@SuppressWarnings("java:S1118") //Supress erreur de sonar
public class AppConfig {

    @Bean
    @Profile("!test")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        File envFile = new File(".env");
        if (envFile.exists()) {
            configurer.setLocation(new FileSystemResource(envFile));
        }
        // Si le fichier .env n'existe pas, on ne configure pas le PropertySourcesPlaceholderConfigurer
        // Les propriétés seront chargées depuis application.properties
        return configurer;
    }

}