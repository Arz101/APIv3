package com.spring.api.API.services;

import com.spring.api.API.Repositories.*;
import com.spring.api.API.models.DTOs.Posts.HashtagsDTO;
import com.spring.api.API.models.DTOs.Posts.PostData;
import com.spring.api.API.models.DTOs.User.UserNode;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@Component
public class SocialDataStore {
    private final IFollowsRepository followsRepository;
    private final IPostsRepository postsRepository;
    private final IHashTagsRepository hashTagsRepository;
    private final ILikeRepository likeRepository;
    private final IPostViewedRepository postViewedRepository;

    private final Map<Long, PostData> postsById = new HashMap<>();
    private final Map<String, Set<PostData>> postsByUsers = new HashMap<>(); // Username is the key and the value of your own posts

    private final Map<Long, Set<String>> hashtagsByPosts = new HashMap<>();
    private final Map<String, Set<PostData>> postsGroupedByTags = new HashMap<>();

    private final Map<String, Map<String, Integer>> hashtagGraph = new HashMap<>();

    private final Map<Long, Set<UserNode>> postsLikesByUsers = new HashMap<>(); //PostId and the users who liked it
    private final Map<UserNode, Set<Long>> usersAndPostsLiked = new HashMap<>(); // Reverse index

    private final Map<UserNode, Map<String, Integer>> usersTags = new HashMap<>(); // Users interest

    private final Map<UserNode, Set<UserNode>> followsGraph = new HashMap<>();

    private final Map<UserNode, Set<Long>> postsIdAlreadyViewedPerUser = new HashMap<>();

    private static final Logger log = LoggerFactory.getLogger(SocialDataStore.class);

    public SocialDataStore(IFollowsRepository followsRepository,
                           IPostsRepository postsRepository,
                           IHashTagsRepository hashTagsRepository,
                           ILikeRepository likeRepository,
                           IPostViewedRepository postViewedRepository){
        this.followsRepository = followsRepository;
        this.postsRepository = postsRepository;
        this.hashTagsRepository = hashTagsRepository;
        this.likeRepository = likeRepository;
        this.postViewedRepository = postViewedRepository;
    }

    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Void> buildFollowsGraph(){
        var follows = this.followsRepository.getAll();
        for (var f : follows){
            this.followsGraph.computeIfAbsent(new UserNode(f.followerId(), f.followerUsername()), k -> new HashSet<>())
                    .add(new UserNode(f.followedId(), f.followedUsername()));
        }

        log.info("Graph charged!");
        log.info("Graph Size: {}",followsGraph.size());
        return CompletableFuture.completedFuture(null);
    }

    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Void> initPosts(){
        var posts = this.postsRepository.getAllPosts();
        var hashtags = this.hashTagsRepository.getAllHashtagsByPosts();
        var likes = this.likeRepository.allLikes();

        for (var post : posts){
            this.postsByUsers.computeIfAbsent(post.username(), u -> new HashSet<>())
                    .add(post);

            if(!this.postsById.containsKey(post.id())){
                this.postsById.put(post.id(), post);
            }
        }

        for(var hash : hashtags){
            this.hashtagsByPosts.computeIfAbsent(hash.postId(), h -> new HashSet<>())
                    .add(hash.name());

            this.postsGroupedByTags.computeIfAbsent(hash.name(), h -> new HashSet<>())
                    .add(this.postsById.get(hash.postId()));
        }

        for(var like : likes){
            UserNode key = new UserNode(like.userId(), like.username());
            this.postsLikesByUsers.computeIfAbsent(like.postId(), l -> new HashSet<>())
                    .add(key);

            this.usersAndPostsLiked.computeIfAbsent(key, l -> new HashSet<>())
                    .add(like.postId());
        }

        this.LoadHashtagsGraph(posts, hashtags);
        return CompletableFuture.completedFuture(null);
    }

    private void LoadHashtagsGraph(@NonNull List<PostData> posts, @NonNull List<HashtagsDTO> hashtags){
        Set<Long> postsIds = posts.stream()
                .map(PostData::id)
                .collect(Collectors.toSet());

        for(var hash: hashtags){
            if (!this.hashtagGraph.containsKey(hash.name())){
                this.hashtagGraph.put(hash.name(), new HashMap<>());
            }
        }

        log.info("total posts: {}", postsIds.size());
        log.info("Graph nodes: {}", this.hashtagGraph.size());

        for (var id : postsIds){
            var hashSet = this.hashtagsByPosts.getOrDefault(id, new HashSet<>());
            if(hashSet.isEmpty() || hashSet.size() == 1) continue;

            var tags = new ArrayList<>(hashSet);

            for (int i = 0; i < tags.size(); i++) {
                for (int j = i + 1; j < tags.size(); j++) {
                    var a = tags.get(i);
                    var b = tags.get(j);

                    this.hashtagGraph
                            .computeIfAbsent(a, k -> new HashMap<>())
                            .merge(b, 1, Integer::sum);
                    this.hashtagGraph
                            .computeIfAbsent(b, k -> new HashMap<>())
                            .merge(a, 1 , Integer::sum);
                }
            }
        }
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> tagLikedPerUser(){
        var listUsers = this.followsGraph.entrySet()
                .stream()
                .flatMap(key ->
                        key.getValue().stream())
                .collect(Collectors.toSet());

        for (var user : listUsers){
            Map<String ,Integer> map = new HashMap<>();

            var posts = this.usersAndPostsLiked.getOrDefault(user, new HashSet<>());

            for (var post : posts){
                var tags = this.hashtagsByPosts.getOrDefault(post, new HashSet<>());

                for (var tag : tags){
                    if (tag.equals("none")) continue;
                    map.merge(tag, 1, Integer::sum);
                }
            }
            this.usersTags.computeIfAbsent(user, u -> new HashMap<>())
                    .putAll(map);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Void> loadPostsViewed(){
        var postsViewed = this.postViewedRepository.getAllPostsViewed();

        for (var post : postsViewed) {
            this.postsIdAlreadyViewedPerUser.computeIfAbsent(
                            new UserNode(post.userId(), post.username()), u -> new HashSet<>())
                    .add(post.postId());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async("taskExecutor")
    public void AddNewPosts(PostData postData, String username, Set<String> tags){
        log.info("Add new post: {}", postData.id());

        postsByUsers.computeIfAbsent(username, u -> new HashSet<>())
                .add(postData);

        log.info("New post has been added: {}", postData.id());

        if(!this.postsById.containsKey(postData.id())){
            this.postsById.put(postData.id(), postData);
        }

        log.info("New post has been added to postsById: {}", postData.id());

        this.hashtagsByPosts.computeIfAbsent(postData.id(), h -> new HashSet<>())
                .addAll(tags);

        log.info("Post tags has been added to hashtagsByPosts: {}", postData.id());

        if(tags.size() > 1) {
            List<String> tagsList = tags.stream().toList();
            for (int i = 0; i < tagsList.size(); i++) {
                for (int j = i + 1; j < tagsList.size(); j++) {
                    var a = tagsList.get(i);
                    var b = tagsList.get(j);

                    this.hashtagGraph
                            .computeIfAbsent(a, k -> new HashMap<>())
                            .merge(b, 1, Integer::sum);
                    this.hashtagGraph
                            .computeIfAbsent(b, k -> new HashMap<>())
                            .merge(a, 1, Integer::sum);
                }
            }
        }

        log.info("All post data has been added: {} successfully", postData.id());
    }

    @Async("taskExecutor")
    public void setLike(String username, Long userId, Long postId){
        var currentUser = new UserNode(userId, username);

        var tags = this.hashtagsByPosts.getOrDefault(postId, new HashSet<>());

        this.usersAndPostsLiked.getOrDefault(currentUser, new HashSet<>())
                .add(postId);

        tags.forEach(t -> this.usersTags.getOrDefault(currentUser, new HashMap<>())
                .merge(t, 1, Integer::sum));

        this.postsLikesByUsers.computeIfAbsent(postId, u -> new HashSet<>())
                .add(currentUser);

        this.postsIdAlreadyViewedPerUser.computeIfAbsent(currentUser, u -> new HashSet<>())
                .add(postId);
    }
}
