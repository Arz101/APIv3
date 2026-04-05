package com.spring.api.API.services;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import com.spring.api.API.Repositories.*;
import com.spring.api.API.models.*;
import com.spring.api.API.models.DTOs.Posts.*;
import com.spring.api.API.models.PostsSaved.PostsSaved;
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
    private final ILikeRepository likeRepository;
    private final IFollowsRepository followsRepository;
    private final IUserRepository userRepository;
    private final IPostViewedRepository postViewedRepository;
    private final IProfileRepository profileRepository;
    private final IHashTagsRepository hashTagsRepository;
    private final StorageService storage;
    private final RankingService rankingService;
    private final IPostsSavedRepository postsSavedRepository;

    public PostsService(IPostsRepository repository,
                        ILikeRepository likeRepository,
                        IFollowsRepository followsRepository,
                        IUserRepository userRepository,
                        IPostViewedRepository postViewedRepository,
                        IProfileRepository profileRepository,
                        IHashTagsRepository hashTagsRepository,
                        StorageService storage,
                        RankingService rankingService,
                        IPostsSavedRepository postsSavedRepository
    ) {
        this.repository = repository;
        this.likeRepository = likeRepository;
        this.followsRepository = followsRepository;
        this.userRepository = userRepository;
        this.postViewedRepository = postViewedRepository;
        this.profileRepository = profileRepository;
        this.hashTagsRepository = hashTagsRepository;
        this.storage = storage;
        this.rankingService = rankingService;
        this.postsSavedRepository = postsSavedRepository;
    }

    @Transactional
    public PostResponse create(@NonNull CreatePostDTO dto, String username) {
        var user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        dto.hashtags().stream().map(h -> h.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")).
                collect(Collectors.toSet());

        Set<Hashtags> hashtags = new HashSet<>();
        if(!dto.hashtags().isEmpty()){
            hashtags = dto.hashtags()
                    .stream().map(name -> this.hashTagsRepository.existsHashtag(name)
                            .orElseGet(() -> this.hashTagsRepository.save(new Hashtags(name))))
                    .collect(Collectors.toSet());

            hashtags.forEach(hashtag -> {
                hashtag.setPosts_count(hashtag.getPosts_count() + 1);
                this.hashTagsRepository.save(hashtag);
            });
        }

        var post = this.repository.save(new Posts(
            dto, this.userRepository.getReferenceById(user_id), hashtags
        ));

        return new PostResponse(
            post.getId(),
            post.getDescription(),
            post.getPicture(),
            username,
            0L,
            0L,
            post.getDatecreated()
        );
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
    public List<PostResponseWithHashtags> getMyPosts(String username) {
        var user_id = this.userRepository.getIdByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Something went wrong"));
        var posts = this.repository.findPosts(user_id);
        return this.transformPostResponse(posts);
    }

    @Transactional(readOnly = true)
    public List<PostResponseWithHashtags> getUserPosts(String target, String currentUser){
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
                        HashtagsDTO::post_id,
                        Collectors.mapping(HashtagsDTO::name, Collectors.toSet())
                ));

        return posts.stream().map(post -> {
            var hashes = hashtags_mapped.getOrDefault(post.id(), Set.of());
            return new PostResponseWithHashtags(
                    post, hashes
            );

        }).collect(Collectors.toList());
    }

    public List<?> feed (@NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return this.rankingService.posts(userId);
    }

    @Transactional
    public Map<String,String> setLike(Long post_id, String username){
        var post = this.repository.getReferenceById(post_id);

        var user_id = this.userRepository.getIdByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        var user = this.userRepository.getReferenceById(user_id);

        var owner = this.userRepository.getReferenceById(post.getUser().getId());

        if (this.profileRepository.isPrivate(owner.getId())) {
            boolean isFollowing = owner.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(user_id));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        if(!this.likeRepository.findLikeByUserAndPost(user_id, post_id)) {
            this.likeRepository.save(new Likes(user, post));
        }

        if (!post.getUser().getId().equals(user_id) &&
            !this.postViewedRepository.alreadyViewed(user_id, post_id)
        ) {
            this.postViewedRepository.save(new PostViewed(post, user));
            return Map.of("message", "Liked");
        }

        return Map.of("message", "Already Liked");
    }

    @Transactional
    public void unlike(Long postId, String username){
        var currentUser = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(""));
        var view = this.postViewedRepository.postView(currentUser, postId);
        view.setLikes(false);
        this.likeRepository.deleteByPostIdAndUserId(postId, currentUser);
    }

    @Transactional
    public Map savePosts(Long postId, @NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        var post = this.repository.getReferenceById(postId);
        var postsOwnerId = post.getUser().getId();

        if (this.profileRepository.isPrivate(postsOwnerId)) {
            if (!this.followsRepository.existsFollow(postsOwnerId, userId)) {
                throw new ProfilePrivateException("This account is private");
            }
        }

        this.postsSavedRepository.save(new PostsSaved(userId, postId));

        if(this.postViewedRepository.alreadyViewed(userId, postId)){
            var pv = this.postViewedRepository.postView(userId, postId);
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

        var pv = this.postViewedRepository.postView(userId, postId);
        pv.setSave(false);
        this.postViewedRepository.save(pv);
        this.postsSavedRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponseWithHashtags> savedPostsList(@NonNull UserDetails user){
        var userId = this.userRepository.getIdByUsername(user.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        var postsIds = this.postsSavedRepository.postsSavedIds(userId);
        var posts = this.repository.getPostsResponseByIdList(postsIds);

        return this.transformPostResponse(posts);
    }

    @Transactional
    public PostResponse updatePost(@NonNull UpdatePostDTO data, Long post_id, String username){
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

        this.likeRepository.deleteAllLikeByPostId(post_id);
        this.repository.delete(post);
    }

    @Transactional(readOnly = true)
    public PostResponse findPostById(Long post_id, String currentUser){
        return this.repository.findPostResponseById(post_id)
            .orElseThrow(() -> new PostNotFoundException("Not found"));
    }

    @Transactional(readOnly = true)
    public PostResponseWithHashtags getPostsWithHashTags(long post_id, String username){
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

        PostResponse postResponse = this.repository.findPostResponseById(post_id)
                .orElseThrow(() -> new PostNotFoundException("Not found"));

        Set<String> hashtags = this.hashTagsRepository.getHashtagsByPostId(post_id);

        return new PostResponseWithHashtags(
                postResponse,
                hashtags
        );
    }

    public List<PostResponseWithHashtags> mostPopularPostsByHashtag(String hashtag, String username){
        var user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        var hashtag_id = this.hashTagsRepository.getIdByName(hashtag)
                .orElseThrow(() -> new RuntimeException("Hashtags not exists or not found"));

        var popular_posts = this.repository.mostPopularPostsByHashtags(user_id, List.of(hashtag_id));

        var posts_response = popular_posts.stream()
                .map(post -> new PostResponse(
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
    public List<PostResponseWithHashtags> popularPostsLikedByFolloweds(String username){
        var user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        var popular_posts = this.repository.mostPopularPostLikedByFollowings(user_id);

        var posts_response = popular_posts.stream()
                .map(post -> new PostResponse(
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
    private List<PostResponseWithHashtags> transformPostResponse(@NonNull List<PostResponse> posts){
        var getPostsIds = posts.stream()
                .map(PostResponse::id)
                .collect(Collectors.toList());

        var hashtags = this.hashTagsRepository.getHashtagsByPostIdList(getPostsIds);

        var hashtags_mapped = hashtags.stream()
                .collect(Collectors.groupingBy(
                        HashtagsDTO::post_id,
                        Collectors.mapping(HashtagsDTO::name, Collectors.toSet())
                ));

        return posts.stream().map(post -> {
            var hashes = hashtags_mapped.getOrDefault(post.id(), Set.of());
            return new PostResponseWithHashtags(
                    post, hashes
            );

        }).collect(Collectors.toList());
    }
}
