package com.spring.api.API.services;

import com.spring.api.API.models.DTOs.Posts.*;
import com.spring.api.API.models.DTOs.User.UserNode;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class FeedService {
    private static final double LIKE = 1.5;
    private static final double COMMENT = 1.0;
    private static final double TAGCOMMON = 2.0;

    private static final Logger log = LoggerFactory.getLogger(FeedService.class);
    private final SocialDataStore store;

    public FeedService(SocialDataStore socialService) {
        this.store = socialService;
    }

    public List<?> getMostHashOccurrencesByHash(String hashtag){
        var map = this.store.getHashtagGraph().getOrDefault(hashtag, new HashMap<>());
        return map.entrySet().stream()
                .sorted(Map.Entry.<String,Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Set<Long> postsILiked(String username, Long userId){
        var currentUser = new UserNode(userId, username);
        return this.store.getPostsLikesByUsers().entrySet().stream()
                .filter(key -> key.getValue()
                        .contains(currentUser))
                .map(p -> p.getKey())
                .collect(Collectors.toSet());
    }

    public Map<UserNode, Set<PostData>> postsLikedByFollows(String username, Long userId){
        var currentUser = new UserNode(userId, username);
        var currentFollows = this.store.getFollowsGraph().getOrDefault(currentUser, new HashSet<>());

        Map<UserNode, Set<PostData>> postsLikedByFollows = new HashMap<>();
        var postsAlreadyViewed = this.store.getPostsIdAlreadyViewedPerUser()
                .getOrDefault(new UserNode(userId, username), new HashSet<>());

        for (var follow : currentFollows){
            var posts = this.store.getUsersAndPostsLiked().getOrDefault(follow, new HashSet<>());
            postsLikedByFollows.computeIfAbsent(follow, u -> new HashSet<>())
                    .addAll(posts.stream()
                            .filter(p -> !postsAlreadyViewed.contains(p))
                            .map(p -> this.store.getPostsById()
                                    .get(p))
                            .collect(Collectors.toSet()));
        }
        return  postsLikedByFollows;
    }

    public List<String> tagsLikedByUser(String username, Long userId){
        var postsLiked = this.postsILiked(username, userId);
        Map <String, Integer> tags = new HashMap<>();

        for (var id : postsLiked){
            var tagsByPost = this.store.getHashtagsByPosts()
                    .getOrDefault(id, new HashSet<>());

            for (var tag : tagsByPost){
                if(tag.equals("none")) continue;
                if(!tags.containsKey(tag)){
                    tags.put(tag, 1);
                } else {
                    tags.put(tag, tags.get(tag) + 1);
                }
            }
        }

        return tags.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private Set<PostResponse> popularPostsByTagOccurrences(@NonNull List<String> tags, Long userId, String username){
        var tagsOccurrences = tags.parallelStream()
                .flatMap(tag -> {
                    var map = this.store.getHashtagGraph().getOrDefault(tag, Map.of());
                    return map.entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(3)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toSet()).stream();
                })
                .collect(Collectors.toSet());
        tagsOccurrences.addAll(tags);

        var popularPostsByTags = tagsOccurrences.stream()
                .flatMap(tag -> this.store.getPostsGroupedByTags().getOrDefault(tag, new HashSet<>())
                        .stream())
                .filter(posts -> !this.store.getPostsIdAlreadyViewedPerUser()
                        .getOrDefault(new UserNode(userId, username), new HashSet<>())
                        .contains(posts.id()))
                .map(to ->
                        new PostResponse(
                                to,
                                this.store.getHashtagsByPosts().getOrDefault(to.id(), new HashSet<>())
                        )
                ).limit(30)
                .collect(Collectors.toSet());
        return popularPostsByTags;
    }

    public List<PostResponse> createFeed(String username, Long userId){
        var tagsILike = this.tagsLikedByUser(username, userId);
        var postsLikedByFollows = this.postsLikedByFollows(username, userId);
        var posts = new ArrayList<>(postRecommendationBasedOnFollowsLikes(postsLikedByFollows, tagsILike));
        posts.addAll(rankPopularPostsByTagsRecommendations(username, userId, postsLikedByFollows,  tagsILike));

        log.info("FEED CREATED SUCCESSFULLY");
        return posts;
    }

    private List<PostResponse> postRecommendationBasedOnFollowsLikes(@NonNull Map<UserNode, Set<PostData>> postsLikedByFollows, List<String> tags){
        Map<PostData, Double> rankPostsLikedByFollows = new HashMap<>();
        for (var key : postsLikedByFollows.entrySet()){
            var posts = key.getValue();
            for (var post : posts){
                double score = (post.likes() * LIKE) + ( post.comments() * COMMENT);
                var currentPostTags = new HashSet<>(
                        this.store.getHashtagsByPosts()
                                .getOrDefault(post.id(), new HashSet<>())
                );
                currentPostTags.retainAll(tags);
                score += currentPostTags.size() * TAGCOMMON;
                rankPostsLikedByFollows.merge(post, score, Double::sum);
            }
        }

        return rankPostsLikedByFollows.entrySet()
                .stream().sorted(Map.Entry.<PostData, Double>comparingByValue().reversed())
                .limit(50)
                .map(post -> {
                    var p = post.getKey();
                    return  new PostResponse(
                            p, this.store.getHashtagsByPosts()
                                    .getOrDefault(p.id(), new HashSet<>())
                    );
                }).toList();
    }

    private List<PostResponse> rankPopularPostsByTagsRecommendations(String username, Long userId,
                                                                     @NonNull Map<UserNode, Set<PostData>> postsLikedByFollows,
                                                                     List<String> tags){
        var popularPosts = this.popularPostsByTagOccurrences(tags, userId, username);
        var setPostsLoF = postsLikedByFollows.entrySet()
                .stream().flatMap(key -> key.getValue().stream())
                .collect(Collectors.toSet());

        Map<PostData, Double> rankPostsByTags = new HashMap<>();

        for (var post : popularPosts){
            double score = (post.post().likes() * LIKE) + ( post.post().comments() * COMMENT);
            if(setPostsLoF.contains(post)) {
                score += 30.0;
            }
            rankPostsByTags.merge(post.post(), score, Double::sum);
        }

        return rankPostsByTags.entrySet()
                .stream().sorted(Map.Entry.<PostData, Double>comparingByValue().reversed())
                .limit(50)
                .map(post -> {
                    var p = post.getKey();
                    return  new PostResponse(
                            p, this.store.getHashtagsByPosts()
                            .getOrDefault(p.id(), new HashSet<>())
                    );
                }).toList();
    }

    public List<PostResponse> timeLine (String username, Long userId){
        var currentUser = new UserNode(userId, username);
        var myFollowings = this.store.getFollowsGraph().getOrDefault(currentUser, Set.of());
        var postsLiked = this.store.getUsersAndPostsLiked().getOrDefault(currentUser, Set.of());

        var posts = myFollowings.stream()
                .flatMap(user -> this.store.getPostsByUsers()
                        .getOrDefault(user.username(), new HashSet<>()).stream())
                .filter(post -> !postsLiked.contains(post.id()))
                .toList();

        Map<PostData, Double> rankPostsByTags = new HashMap<>();

        for (var post : posts){
            double score = (post.likes() * LIKE) + ( post.comments() * COMMENT);
            rankPostsByTags.merge(post, score, Double::sum);
        }

        return rankPostsByTags.entrySet().stream()
                .sorted(Map.Entry.<PostData, Double>comparingByValue().reversed())
                .map(key -> new PostResponse(key.getKey(), this.store.getHashtagsByPosts()
                        .getOrDefault(key.getKey().id(), new HashSet<>())))
                .limit(50)
                .toList();
    }

    public List<PostResponse> mostPopularPostsByHashtagRanked(String tag, String username, Long  userId) {
        var currentUser =  new UserNode(userId, username);

        var posts = this.store.getPostsGroupedByTags().getOrDefault(tag, Set.of());
        var postsLiked = this.store.getUsersAndPostsLiked().getOrDefault(currentUser, Set.of());
        var myFollowings = this.store.getFollowsGraph().getOrDefault(currentUser, Set.of());

        var postsLikedByFollows = this.store.getPostsLikesByUsers().getOrDefault(currentUser, Set.of());

        posts.stream()
                .filter(post -> !postsLiked.contains(post.id()))
                .toList();

        Map<PostData, Double> rankPosts = new HashMap<>();
        for (var post : posts){

            double score = (post.likes() * LIKE) + ( post.comments() * COMMENT);
            rankPosts.merge(post, score, Double::sum);
        }
        return List.of();
    }
}
