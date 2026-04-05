package com.spring.api.API.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.spring.api.API.models.DTOs.User.UserRanking;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
public class AppConfig {

    @Bean
    public ExecutorService taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public Cache<Long, UserRanking> userRankingCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(30, MINUTES)
                .build();
    }
}
