package com.spring.api.API.services;

import com.spring.api.API.Repositories.*;
import com.spring.api.API.models.Likes.Likes;
import com.spring.api.API.models.PostViewed.PostViewed;
import com.spring.api.API.security.Exceptions.PostNotFoundException;
import com.spring.api.API.security.Exceptions.ProfilePrivateException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class LikesService {

    private final ILikeRepository  likeRepository;
    private final IUserRepository userRepository;
    private final IPostViewedRepository postViewedRepository;
    private final IPostsRepository postsRepository;
    private final IProfileRepository profileRepository;
    private final IFollowsRepository followsRepository;
    private final SocialDataStore store;

    public LikesService(ILikeRepository likeRepository,
                        IUserRepository userRepository,
                        IPostViewedRepository postViewedRepository,
                        IPostsRepository postsRepository,
                        IProfileRepository profileRepository,
                        IFollowsRepository followsRepository,
                        SocialDataStore store) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.postViewedRepository = postViewedRepository;
        this.postsRepository = postsRepository;
        this.profileRepository = profileRepository;
        this.followsRepository = followsRepository;
        this.store = store;
    }

    @Transactional
    public Map<String,String> setLike(Long postId, String username){
        var post = this.postsRepository.getReferenceById(postId);

        var userId = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        var userRef = this.userRepository.getReferenceById(userId);
        var owner = this.userRepository.getReferenceById(post.getUser().getId());

        if (this.profileRepository.isPrivate(owner.getId())) {
            boolean isFollowing = this.followsRepository.isFollowOf(userId, owner.getId());

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        if(!this.likeRepository.findLikeByUserAndPost(userId, postId)) {
            this.store.setLike(username, userId, postId);
            this.likeRepository.save(new Likes(userRef, post));
        }

        if (!post.getUser().getId().equals(userId) &&
                !this.postViewedRepository.alreadyViewed(userId, postId)
        ) {
            this.postViewedRepository.save(new PostViewed(userRef, post));
            return Map.of("message", "Liked");
        }

        return Map.of("message", "Already Liked");
    }

    @Transactional
    public void unlike(Long postId, String username){
        var currentUser = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(""));

        var view = this.postViewedRepository.postView(currentUser, postId)
                .orElseThrow(() -> new PostNotFoundException("Something went wrong!"));

        view.setLikes(false);
        this.likeRepository.deleteByPostIdAndUserId(postId, currentUser);
    }

}
