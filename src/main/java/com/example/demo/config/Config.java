package com.example.demo.config;

import com.github.javafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {
    @Bean(name = "restTemplateShort")
    public RestTemplate getRestTemplateShort() {
        System.out.println("getRestTemplateShort");
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3 * 1000);
        requestFactory.setReadTimeout(3 * 1000);
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    @Bean(name = "restTemplateLong")
    public RestTemplate getRestTemplateLong() {
        System.out.println("getRestTemplateLong");
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10 * 1000);
        requestFactory.setReadTimeout(10 * 1000);
        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }

    @Bean
    public Faker faker(){
        return new Faker();
    }
}
