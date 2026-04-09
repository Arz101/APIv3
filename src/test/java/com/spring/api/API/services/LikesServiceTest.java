package com.spring.api.API.services;

import com.spring.api.API.Repositories.*;
import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import com.spring.api.API.models.Likes.Likes;
import com.spring.api.API.models.Posts;
import com.spring.api.API.models.User;
import com.spring.api.API.security.Exceptions.ProfilePrivateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikesServiceTest {

    @Mock private ILikeRepository likeRepository;
    @Mock private IUserRepository userRepository;
    @Mock private IPostViewedRepository postViewedRepository;
    @Mock private IPostsRepository postsRepository;
    @Mock private IProfileRepository profileRepository;
    @Mock private IFollowsRepository followsRepository;

    @InjectMocks
    private LikesService likesService;

    @Test
    void setLikeWhenPostIsPublic(){
        Long postId = 1L;
        Long userId = 10L;
        String username = "arz";

        User currentUser = new User(username, "test", "12345", "active");
        currentUser.setId(1L);

        User owner = new User("any", "email@test.com", "12345", "active");
        owner.setId(99L);

        Posts post = new Posts(new CreatePostDTO("Test", "default", Set.of()),
                owner, Set.of());
        post.setId(postId);

        when(userRepository.getIdByUsername(username)).thenReturn(Optional.of(userId));
        when(postsRepository.getReferenceById(postId)).thenReturn(post);
        when(userRepository.getReferenceById(userId)).thenReturn(currentUser);
        when(userRepository.getReferenceById(post.getUser().getId())).thenReturn(owner);
        when(profileRepository.isPrivate(owner.getId())).thenReturn(false);
        when(likeRepository.findLikeByUserAndPost(userId, postId)).thenReturn(false);
        when(postViewedRepository.alreadyViewed(userId, postId)).thenReturn(false);

        var result = likesService.setLike(postId, username);
        assertThat(result).containsEntry("message", "Liked");
        verify(likeRepository).save(any(Likes.class));
    }

    @Test
    void setLikeWhenAlreadyLiked(){
        Long postId = 1L;
        Long userId = 10L;
        String username = "arz";

        User currentUser = new User(username, "test", "12345", "active");
        currentUser.setId(1L);

        User owner = new User("any", "email@test.com", "12345", "active");
        owner.setId(99L);

        Posts post = new Posts(new CreatePostDTO("Test", "default", Set.of()),
                owner, Set.of());
        post.setId(postId);

        when(userRepository.getIdByUsername(username)).thenReturn(Optional.of(userId));
        when(postsRepository.getReferenceById(postId)).thenReturn(post);
        when(userRepository.getReferenceById(userId)).thenReturn(currentUser);
        when(userRepository.getReferenceById(post.getUser().getId())).thenReturn(owner);
        when(profileRepository.isPrivate(owner.getId())).thenReturn(false);
        when(likeRepository.findLikeByUserAndPost(userId, postId)).thenReturn(true);
        when(postViewedRepository.alreadyViewed(userId, postId)).thenReturn(true);

        var result = likesService.setLike(postId, username);
        assertThat(result).containsEntry("message", "Already Liked");
        verify(likeRepository, never()).save(any());
    }

    @Test
    void setLikeWhenProfileIsPrivateAndNotFollowing_ThrowException(){
        Long postId = 1L;
        Long userId = 10L;
        String username = "arz";

        User owner = new User("any", "email@test.com", "12345", "active");
        owner.setId(99L);
        owner.setFollowers(new ArrayList<>());

        Posts post = new Posts(new CreatePostDTO("Test", "default", Set.of()),
                owner, Set.of());
        post.setId(postId);

        when(postsRepository.getReferenceById(postId)).thenReturn(post);
        when(userRepository.getIdByUsername(username)).thenReturn(Optional.of(userId));
        when(userRepository.getReferenceById(userId)).thenReturn(new User());
        when(userRepository.getReferenceById(post.getUser().getId())).thenReturn(owner);
        when(profileRepository.isPrivate(owner.getId())).thenReturn(true);
        when(followsRepository.isFollowOf(userId, owner.getId())).thenReturn(false);

        assertThrows(ProfilePrivateException.class,
                () -> likesService.setLike(postId, username)
        );
    }

    @Test
    void setLikeWhenProfileIsPrivateAndFollowing_ReturnLiked(){
        Long postId = 1L;
        Long userId = 10L;
        String username = "arz";

        User owner = new User("any", "email@test.com", "12345", "active");
        owner.setId(99L);
        owner.setFollowers(new ArrayList<>());

        Posts post = new Posts(new CreatePostDTO("Test", "default", Set.of()),
                owner, Set.of());
        post.setId(postId);

        when(postsRepository.getReferenceById(postId)).thenReturn(post);
        when(userRepository.getIdByUsername(username)).thenReturn(Optional.of(userId));
        when(userRepository.getReferenceById(userId)).thenReturn(new User());
        when(userRepository.getReferenceById(post.getUser().getId())).thenReturn(owner);
        when(profileRepository.isPrivate(owner.getId())).thenReturn(true);
        when(followsRepository.isFollowOf(userId, owner.getId())).thenReturn(true);
        when(likeRepository.findLikeByUserAndPost(userId, postId)).thenReturn(false);
        when(postViewedRepository.alreadyViewed(userId, postId)).thenReturn(false);

        var result = likesService.setLike(postId, username);
        assertThat(result).containsEntry("message", "Liked");
        verify(likeRepository).save(any(Likes.class));
    }
}
