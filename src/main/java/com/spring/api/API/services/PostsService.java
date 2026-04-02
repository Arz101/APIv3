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
import org.springframework.stereotype.Service;
import com.spring.api.API.security.Exceptions.PostNotFoundException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PostsService {
    private final IPostsRepository repository;
    private final ILikeRepository likeRepository;
    private final IFollowsRepository followsRepository;
    private final IUserRepository userRepository;
    private final IPostViewedRepository postViewedRepository;
    private final IProfileRepository profileRepository;
    private final IHashTagsRepository hashTagsRepository;
    private static final Logger log = LoggerFactory.getLogger(PostsService.class);

    public PostsService(IPostsRepository repository,
        ILikeRepository likeRepository,
        IFollowsRepository followsRepository,
        IUserRepository userRepository,
        IPostViewedRepository postViewedRepository,
        IProfileRepository profileRepository,
        IHashTagsRepository hashTagsRepository
    ) {
        this.repository = repository;
        this.likeRepository = likeRepository;
        this.followsRepository = followsRepository;
        this.userRepository = userRepository;
        this.postViewedRepository = postViewedRepository;
        this.profileRepository = profileRepository;
        this.hashTagsRepository = hashTagsRepository;
    }

    @Transactional
    public PostResponse create(@NonNull CreatePostDTO dto, String username) {
        var user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        dto.hashtags().stream().map(h -> {
            String normalized = h.toLowerCase()
                    .replaceAll("[áàäâ]", "a")
                    .replaceAll("[éèëê]", "e")
                    .replaceAll("[íìïî]", "i")
                    .replaceAll("[óòöô]", "o")
                    .replaceAll("[úùüû]", "u");
            return normalized;
        }).collect(Collectors.toSet());

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
    public List<?> feed(String username) {
        var userIds = this.followsRepository.findFollowedUserIdsByFollowerUsername(username);
        Pageable pageable = PageRequest.of(0, 50);

        var hashtags = this.hashTagsRepository.getAllHashtagsByFollowingsId(userIds);
        var posts = this.repository.findFeed(userIds, pageable);

        var hashtags_mapped = hashtags.stream()
                .collect(Collectors.groupingBy(
                        HashtagsDTO::post_id,
                        Collectors.mapping(HashtagsDTO::name, Collectors.toSet())
                ));

        var full_posts = posts.stream().map(post -> {
            var hashes = hashtags_mapped.getOrDefault(post.id(), Set.of());
            return new PostResponseWithHashtags(
                    post, hashes
            );

        }).collect(Collectors.toList());
        return full_posts;
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
    public String deletePost(Long post_id, String username){
        Long user_id = this.userRepository.getIdByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Not found"));

        Posts post = this.repository.findPostByUserAndPostId(user_id, post_id)
            .orElseThrow(() -> new PostsActionsUnauthorized("Unauthorized Action"));
        
        try{
            this.likeRepository.deleteByPostId(post_id);

            this.repository.delete(post);
            return "Successfully!";
        }
        catch(RuntimeException e){
            return "Something went wrong!";
        }
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

        var popular_posts = this.repository.mostPopularPostsByHashtag(user_id, hashtag_id);

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
            boolean isFollowing = targetUser.getFollowers()
                    .stream()
                    .anyMatch(t -> t.getFollower().getId().equals(user_id));

            if (!isFollowing) {
                throw new ProfilePrivateException("This account is private");
            }
        }
        return targetUser.getId();
    }

    @Transactional(readOnly = true) // Transform PostResponse for add Hashtags
    private List<PostResponseWithHashtags> transformPostResponse(@NonNull List<PostResponse> posts){
        var getPostsIds = posts.stream()
                .map(post -> post.id())
                .collect(Collectors.toList());

        var hashtags = this.hashTagsRepository.getHashtagsByPostIdList(getPostsIds);

        var hashtags_mapped = hashtags.stream()
                .collect(Collectors.groupingBy(
                        HashtagsDTO::post_id,
                        Collectors.mapping(HashtagsDTO::name, Collectors.toSet())
                ));

        var full_posts = posts.stream().map(post -> {
            var hashes = hashtags_mapped.getOrDefault(post.id(), Set.of());
            return new PostResponseWithHashtags(
                    post, hashes
            );

        }).collect(Collectors.toList());
        return full_posts;
    }

}
