package com.spring.api.API.profiles.dtos;

public record ProfileStats(
        Long posts,
        Long followers,
        Long followeds,
        Long requests
) {}

