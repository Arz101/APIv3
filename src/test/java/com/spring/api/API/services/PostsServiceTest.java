package com.spring.api.API.services;

import com.spring.api.API.Repositories.*;
import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import com.spring.api.API.models.Hashtags;
import com.spring.api.API.models.Posts;
import com.spring.api.API.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostsServiceTest {

    @Mock private IPostsRepository repository;
    @Mock private IFollowsRepository followsRepository;
    @Mock private IUserRepository userRepository;
    @Mock private IProfileRepository profileRepository;
    @Mock private IHashTagsRepository hashTagsRepository;
    @Mock private StorageService storage;
    @Mock private FeedService feedService;
    @Mock private CacheAsyncHelper cacheAsyncHelper;
    @Mock private SocialDataStore store;

    @InjectMocks
    private PostsService postsService;

    @Test
    public void createNewPost(){
        Long userId = 1L;
        Set<String> hashtags = Set.of("java", "test");
        var newPost = new CreatePostDTO("Test", "default", hashtags);
        String username = "arz";

        Posts savedPost = new Posts(newPost, new User(), Set.of());
        savedPost.setId(1L);

        Hashtags savedHashtag = new Hashtags("java");
        savedHashtag.setPosts_count(0L);

        when(userRepository.getIdByUsername(username)).thenReturn(Optional.of(userId));
        when(userRepository.getReferenceById(userId)).thenReturn(new User());
        when(hashTagsRepository.existsHashtag(anyString())).thenReturn(Optional.empty());
        when(hashTagsRepository.save(any(Hashtags.class))).thenReturn(savedHashtag);
        when(repository.save(any(Posts.class))).thenReturn(savedPost);

        var result = postsService.create(newPost, username);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(repository).save(any(Posts.class));
        verify(hashTagsRepository, times(hashtags.size() + 1)).save(any(Hashtags.class));
    }

    @Test
    public void getAllPostsByUser(){
        Long userId = 1L;
        String username = "arz";
        Set<String> hashtags = Set.of("java", "test");
        var newPost = new CreatePostDTO("Test", "default", hashtags);
        Posts myPost = new Posts(newPost, new User(), Set.of());
        myPost.setId(1L);

        when(userRepository.getIdByUsername(username)).thenReturn(Optional.of(userId));
    }
}
