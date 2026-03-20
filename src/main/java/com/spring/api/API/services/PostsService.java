package com.spring.api.API.services;

import java.util.List;
import org.springframework.stereotype.Service;
import com.spring.api.API.Repositories.IFollowsRepository;
import com.spring.api.API.Repositories.ILikeRepository;
import com.spring.api.API.Repositories.IPostsRepository;
import com.spring.api.API.Repositories.IProfileRepository;
import com.spring.api.API.models.Posts;
import com.spring.api.API.models.Profiles;
import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import com.spring.api.API.models.DTOs.Posts.PostResponse;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import jakarta.transaction.Transactional;

@Service
public class PostsService {
    
    private final IPostsRepository repository;
    private final IProfileRepository profileRepository;
    private final ILikeRepository likeRepository;
    private final IFollowsRepository followsRepository;

    public PostsService(IPostsRepository repository,
        IProfileRepository profileRepository,
        ILikeRepository likeRepository,
        IFollowsRepository followsRepository
    ) {
        this.repository = repository;
        this.profileRepository = profileRepository;
        this.likeRepository = likeRepository;
        this.followsRepository = followsRepository;
    }

    @Transactional
    public PostResponse create(CreatePostDTO dto, String username) {
        Profiles profile = this.profileRepository.findProfilesByUserUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        Posts post = this.repository.save(new Posts(
            dto,
            profile
        ));

        return new PostResponse(
            post.getId(),
            post.getDescription(),
            post.getPicture(),
            username,
            0L,
            post.getDatecreated()
        );
    }

    public List<PostResponse> getPostsByUsername(String username) {
        List<Posts> posts = this.repository.findByUsername(username);
        
        return posts.stream().map(post -> new PostResponse(
            post.getId(),
            post.getDescription(),
            post.getPicture(),
            username,
            this.likeRepository.countLikesByPostId(post.getId()),
            post.getDatecreated()
        )).toList();
    }

    public List<PostResponse> feed(String username) {
        List<Long> userIds = this.followsRepository.findFollowedUserIdsByFollowerUsername(username);
        Pageable pageable = PageRequest.of(0, 100);
        return this.repository.findFeed(userIds, pageable);
    }
}
