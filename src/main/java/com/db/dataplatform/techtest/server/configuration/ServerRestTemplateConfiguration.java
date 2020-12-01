package com.db.dataplatform.techtest.server.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * this is just to demonstrate that in a real world scenario,
 * the server should have a bespoke configuration.
 * For instance, we might want to tweak connection timeout / read timeout
 * given the instability of the hadoop server.
 *
 * */

@Configuration
@RequiredArgsConstructor
public class ServerRestTemplateConfiguration {
    private final RestTemplateBuilder restTemplateBuilder;

    @Bean(name = "serverRestTemplate")
    public RestTemplate createRestTemplate() {
        return restTemplateBuilder.build();
    }

}
