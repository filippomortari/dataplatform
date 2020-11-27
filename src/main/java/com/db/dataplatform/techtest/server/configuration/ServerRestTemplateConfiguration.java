package com.db.dataplatform.techtest.server.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class ServerRestTemplateConfiguration {
    private final RestTemplateBuilder restTemplateBuilder;

    @Bean(name = "serverRestTemplate")
    public RestTemplate createRestTemplate() {

        return restTemplateBuilder.build();
    }

}
