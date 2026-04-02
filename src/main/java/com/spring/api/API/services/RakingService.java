package com.spring.api.API.services;

import org.springframework.stereotype.Service;

@Service
public class RakingService {

    public final PostsService service;
    public RakingService(PostsService postsService){
        this.service = postsService;
    }
}
