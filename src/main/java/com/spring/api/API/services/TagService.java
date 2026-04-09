package com.spring.api.API.services;

import com.spring.api.API.Repositories.IHashTagsRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final IHashTagsRepository hashTagsRepository;
    private final IUserRepository userRepository;
    private final FeedService feedService;

    public TagService(IHashTagsRepository hashTagsRepository,
                      IUserRepository userRepository,
                      FeedService feedService) {
        this.hashTagsRepository = hashTagsRepository;
        this.userRepository = userRepository;
        this.feedService = feedService;
    }

    public List<?> tagsLikedByUser(@NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        return this.feedService.tagsLikedByUser(user.getUsername(), userId);
    }

    public List<?> getMostHashOccurrencesByHash(String name){
        return this.feedService.getMostHashOccurrencesByHash(name);
    }
}
