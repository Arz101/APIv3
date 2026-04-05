package com.spring.api.API.services;

import com.github.benmanes.caffeine.cache.Cache;
import com.spring.api.API.Repositories.IHashTagsRepository;
import com.spring.api.API.Repositories.IPostsRepository;
import com.spring.api.API.models.DTOs.Posts.PostProjection;
import com.spring.api.API.models.DTOs.Posts.PostResponse;
import com.spring.api.API.models.DTOs.User.UserRanking;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingService {

    private final IPostsRepository repository;
    private final IHashTagsRepository hashTagsRepository;
    private static final Logger log = LoggerFactory.getLogger(RankingService.class);
    private final Cache<Long, UserRanking> userRankingCache;


    public RankingService(IPostsRepository repository,
                          IHashTagsRepository hashTagsRepository,
                          Cache<Long, UserRanking> userRankingCache){
        this.repository = repository;
        this.hashTagsRepository = hashTagsRepository;
        this.userRankingCache = userRankingCache;
    }

    @Async
    public void init(Long userId){
        List<PostResponse> posts = new ArrayList<>();
        List<Long> hashtagsInterests = new ArrayList<>();

        log.info("INIT RANKING POSTS FOR USER {}", userId);

        hashtagsInterests.addAll(this.getUserInterests(userId));
        posts.addAll(this.getMostPopularPostLikedByFollowings(userId));
        posts.addAll(getMostPopularPostsByHashtag(userId, hashtagsInterests));

        userRankingCache.put(userId, new UserRanking(posts));
    }

    public UserRanking initManual(Long userId){
        List<PostResponse> posts = new ArrayList<>();
        List<Long> hashtagsInterests = new ArrayList<>();

        log.info("MANUAL INIT RANKING POSTS FOR USER {}", userId);

        hashtagsInterests.addAll(this.getUserInterests(userId));
        posts.addAll(this.getMostPopularPostLikedByFollowings(userId));
        posts.addAll(getMostPopularPostsByHashtag(userId, hashtagsInterests));

        return new UserRanking(posts);
    }

    @Transactional(readOnly = true)
    private List<PostResponse> getMostPopularPostsByHashtag(Long userId, List<Long> hashtagsInterests){
        var p = this.repository.mostPopularPostsByHashtags(userId,hashtagsInterests);
        return this.addAll(p);
    }

    @Transactional(readOnly = true)
    private List<PostResponse> getMostPopularPostLikedByFollowings(Long userId){
        var p = this.repository.mostPopularPostLikedByFollowings(userId);
        return this.addAll(p);
    }

    @Transactional(readOnly = true)
    private List<Long> getUserInterests(Long userId){
        var p = this.hashTagsRepository.getPrincipalInterestsBasedOnHashtags(userId);
        return p.stream().map(hash -> hash.getHashtags())
                        .collect(Collectors.toList());
    }

    private List<PostResponse> addAll(@NonNull List<PostProjection> p){
        return p.stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getDescription(),
                        post.getPicture(),
                        post.getUsername(),
                        post.getLikes(),
                        post.getComments(),
                        post.getDatecreated().atOffset(ZoneOffset.UTC)
                )).collect(Collectors.toList());
    }

    public List<PostResponse> posts(Long userId){
        var posts = this.userRankingCache.getIfPresent(userId);

        if(posts == null){
            return this.userRankingCache.get(userId, id -> initManual(id)).getFeed();
        }
        return posts.getFeed();
    }
}
