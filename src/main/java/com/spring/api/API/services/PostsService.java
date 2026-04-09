package com.spring.api.API.services;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import com.spring.api.API.Repositories.*;
import com.spring.api.API.models.*;
import com.spring.api.API.models.DTOs.Posts.*;
import com.spring.api.API.security.Exceptions.PostsActionsUnauthorized;
import com.spring.api.API.security.Exceptions.ProfilePrivateException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.spring.api.API.security.Exceptions.PostNotFoundException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostsService {
    private final IPostsRepository repository;
    private final IFollowsRepository followsRepository;
    private final IUserRepository userRepository;
    private final IProfileRepository profileRepository;
    private final IHashTagsRepository hashTagsRepository;
    private final StorageService storage;
    private final FeedService feedService;
    private final CacheAsyncHelper cacheAsyncHelper;
    private final SocialDataStore store;

    public PostsService(IPostsRepository repository,
                        IFollowsRepository followsRepository,
                        IUserRepository userRepository,
                        IProfileRepository profileRepository,
                        IHashTagsRepository hashTagsRepository,
                        StorageService storage,
                        FeedService feed,
                        CacheAsyncHelper cacheAsyncHelper,
                        SocialDataStore store) {
        this.repository = repository;
        this.followsRepository = followsRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.hashTagsRepository = hashTagsRepository;
        this.storage = storage;
        this.feedService = feed;
        this.cacheAsyncHelper = cacheAsyncHelper;
        this.store = store;
    }

    @Transactional
    public PostData create(@NonNull CreatePostDTO dto, String username) {
        var user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        var userRef = this.userRepository.getReferenceById(user_id);

        Set<Hashtags> hashtags = new HashSet<>();
        if(!dto.hashtags().isEmpty()){
            hashtags = dto.hashtags()
                    .stream().map(name -> {
                        var o = name.toLowerCase()
                                .replaceAll("[áàäâ]", "a")
                                .replaceAll("[éèëê]", "e")
                                .replaceAll("[íìïî]", "i")
                                .replaceAll("[óòöô]", "o")
                                .replaceAll("[úùüû]", "u");

                        return this.hashTagsRepository.existsHashtag(name)
                                .orElseGet(() -> this.hashTagsRepository.save(new Hashtags(o)));
                    })
                    .collect(Collectors.toSet());

            hashtags.forEach(hashtag -> {
                hashtag.setPosts_count(hashtag.getPosts_count() + 1);
                this.hashTagsRepository.save(hashtag);
            });
        }

        var post = this.repository.save(new Posts(
            dto, userRef, hashtags
        ));

        var postData = new PostData(
                post.getId(), post.getDescription(), post.getPicture(),
                username, 0L, 0L, post.getDatecreated()
        );

        this.store.AddNewPosts(postData, username, hashtags.stream()
                .map(Hashtags::getName).collect(Collectors.toSet()));

        return postData;
    }

    public void attachImage(Long postId, MultipartFile file, @NonNull UserDetails user){
        var post = this.repository.getReferenceById(postId);

        if(!post.getUser().getUsername().equals(user.getUsername())){
            throw new PostsActionsUnauthorized("Unauthorized");
        }
        String filename = this.storage.save(file);
        post.setPicture(filename);
        this.repository.save(post);
    }

    @Transactional(readOnly = true)
    public List<?> getMyPosts(String username) {
        var user_id = this.userRepository.getIdByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Something went wrong"));
        var posts = this.feedService.getPostsByMap(username);
        return posts;
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getUserPosts(String target, String currentUser){
        var target_id = this.verifyPrivateProfile(target, currentUser);
        var posts = this.repository.findPosts(target_id);
        return this.transformPostResponse(posts);
    }

    @Transactional(readOnly = true)
    public List<?> timeLine(String username) {
        var userIds = this.followsRepository.findFollowedUserIdsByFollowerUsername(username);
        Pageable pageable = PageRequest.of(0, 50);

        var hashtags = this.hashTagsRepository.getAllHashtagsByFollowingsId(userIds);
        var posts = this.repository.findFeed(userIds, pageable);

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

    public List<PostResponse> feed (@NonNull UserDetails user, int page, int size){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return this.cacheAsyncHelper.posts(user.getUsername(), userId, page, size);
    }

    @Transactional
    public PostData updatePost(@NonNull UpdatePostDTO data, Long post_id, String username){
        Long user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        Posts post = this.repository.findPostByUserAndPostId(user_id, post_id)
                .orElseThrow(() -> new PostsActionsUnauthorized("Unauthorized Action"));

        post.setDescription(data.description());
        
        this.repository.save(post);
        return this.repository.findPostResponseById(post.getId())
                .orElseThrow(() -> new PostNotFoundException("Something went wrong"));
    }

    @Transactional
    public void deletePost(Long post_id, String username){
        Long user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        Posts post = this.repository.findPostByUserAndPostId(user_id, post_id)
            .orElseThrow(() -> new PostsActionsUnauthorized("Unauthorized Action"));

        this.repository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostData findPostById(Long post_id, String currentUser){
        return this.repository.findPostResponseById(post_id)
            .orElseThrow(() -> new PostNotFoundException("Not found"));
    }

    @Transactional(readOnly = true)
    public PostResponse getPostsWithHashTags(long post_id, String username){
        Long current_user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Posts target_posts = this.repository.getReferenceById(post_id);
        User owner_posts = this.userRepository.getReferenceById(target_posts.getUser().getId());

        if (this.profileRepository.isPrivate(target_posts.getUser().getId())) {
            boolean isFollowing = owner_posts.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(current_user_id));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        PostData postResponse = this.repository.findPostResponseById(post_id)
                .orElseThrow(() -> new PostNotFoundException("Not found"));

        Set<String> hashtags = this.hashTagsRepository.getHashtagsByPostId(post_id);

        return new PostResponse(
                postResponse,
                hashtags
        );
    }

    public List<PostResponse> mostPopularPostsByHashtag(String hashtag, String username){
        var user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        var hashtag_id = this.hashTagsRepository.getIdByName(hashtag)
                .orElseThrow(() -> new RuntimeException("Hashtags not exists or not found"));

        var popular_posts = this.repository.mostPopularPostsByHashtags(user_id, List.of(hashtag_id));

        var posts_response = popular_posts.stream()
                .map(post -> new PostData(
                        post.getId(),
                        post.getDescription(),
                        post.getPicture(),
                        post.getUsername(),
                        post.getLikes(),
                        post.getComments(),
                        post.getDatecreated().atOffset(ZoneOffset.UTC)
                )).collect(Collectors.toList());

        return this.transformPostResponse(posts_response);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> popularPostsLikedByFolloweds(String username){
        var user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        var popular_posts = this.repository.mostPopularPostLikedByFollowings(user_id);

        var posts_response = popular_posts.stream()
                .map(post -> new PostData(
                        post.getId(),
                        post.getDescription(),
                        post.getPicture(),
                        post.getUsername(),
                        post.getLikes(),
                        post.getComments(),
                        post.getDatecreated().atOffset(ZoneOffset.UTC)
                )).collect(Collectors.toList());

        return this.transformPostResponse(posts_response);
    }

    @Transactional(readOnly = true)
    private Long verifyPrivateProfile(String target, String currentUser){
        User targetUser = this.userRepository.findByUsername(target)
                .orElseThrow();

        Long user_id = this.userRepository.getIdByUsername(currentUser)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        if (this.profileRepository.isPrivate(targetUser.getId())) {
            if (!this.followsRepository.existsFollow(targetUser.getId(), user_id)) {
                throw new ProfilePrivateException("This account is private");
            }
        }
        return targetUser.getId();
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

    public List<?> testRanking(@NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        return this.feedService.postsRecommendations(user.getUsername(), userId);
    }
}
