package com.spring.api.API.services;

import com.spring.api.API.Repositories.*;
import com.spring.api.API.models.DTOs.Posts.HashtagsDTO;
import com.spring.api.API.models.DTOs.Posts.PostData;
import com.spring.api.API.models.DTOs.Posts.PostResponse;
import com.spring.api.API.models.PostsSaved.PostsSaved;
import com.spring.api.API.security.Exceptions.PostNotFoundException;
import com.spring.api.API.security.Exceptions.ProfilePrivateException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostSavedService {

    private final IPostsSavedRepository postsSavedRepository;
    private final IUserRepository userRepository;
    private final IPostsRepository postsRepository;
    private final IProfileRepository profileRepository;
    private final IFollowsRepository followsRepository;
    private final IPostViewedRepository postViewedRepository;
    private final IHashTagsRepository hashTagsRepository;

    public PostSavedService(IPostsSavedRepository postsSavedRepository,
                            IUserRepository userRepository,
                            IPostsRepository postsRepository,
                            IProfileRepository profileRepository,
                            IFollowsRepository followsRepository,
                            IPostViewedRepository postViewedRepository,
                            IHashTagsRepository hashTagsRepository) {
        this.postsSavedRepository = postsSavedRepository;
        this.userRepository = userRepository;
        this.postsRepository = postsRepository;
        this.profileRepository = profileRepository;
        this.followsRepository = followsRepository;
        this.postViewedRepository = postViewedRepository;
        this.hashTagsRepository = hashTagsRepository;
    }

    @Transactional
    public Map savePosts(Long postId, @NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        var post = this.postsRepository.getReferenceById(postId);
        var postsOwnerId = post.getUser().getId();

        if (this.profileRepository.isPrivate(postsOwnerId)) {
            if (!this.followsRepository.existsFollow(postsOwnerId, userId)) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        this.postsSavedRepository.save(new PostsSaved(userId, postId));

        if(this.postViewedRepository.alreadyViewed(userId, postId)){
            var pv = this.postViewedRepository.postView(userId, postId)
                    .orElseThrow(() -> new PostNotFoundException("Something went wrong!"));
            pv.setSave(true);
            this.postViewedRepository.save(pv);
        }
        return Map.of("message", "Post Saved Successfully!");
    }

    @Transactional
    public void unsavePosts(Long postId, @NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        var post = this.postsSavedRepository.postsSavedByUserIdAndPostsId(userId, postId)
                .orElseThrow(() -> new PostNotFoundException("Something went wrong"));

        var pv = this.postViewedRepository.postView(userId, postId)
                .orElseThrow(()  -> new PostNotFoundException("Something went wrong!"));
        pv.setSave(false);
        this.postViewedRepository.save(pv);
        this.postsSavedRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> savedPostsList(@NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        var postsIds = this.postsSavedRepository.postsSavedIds(userId);
        var posts = this.postsRepository.getPostsResponseByIdList(postsIds);

        return this.transformPostResponse(posts);
    }

    @Transactional(readOnly = true) // Transform PostResponse for add Hashtags
    private List<PostResponse> transformPostResponse(@NonNull List<PostData> posts){
        var getPostsIds = posts.stream()
                .map(PostData::id)
                .collect(Collectors.toList());

        var hashtags = this.hashTagsRepository.getHashtagsByPostIdList(getPostsIds);

        var hashtags_mapped = hashtags.stream()
                .collect(Collectors.groupingBy(
                        HashtagsDTO::postId,
                        Collectors.mapping(HashtagsDTO::name, Collectors.toSet())
                ));

        return posts.stream().map(post -> {
            var hashes = hashtags_mapped.getOrDefault(post.id(), Set.of());
            return new PostResponse(
                    post, hashes
            );

        }).collect(Collectors.toList());
    }

}
