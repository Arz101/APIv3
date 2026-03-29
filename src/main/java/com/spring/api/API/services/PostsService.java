package com.spring.api.API.services;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.spring.api.API.Repositories.IFollowsRepository;
import com.spring.api.API.Repositories.ILikeRepository;
import com.spring.api.API.Repositories.IPostViewedRepository;
import com.spring.api.API.Repositories.IPostsRepository;
import com.spring.api.API.Repositories.IProfileRepository;
import com.spring.api.API.Repositories.IUserRepository;
import com.spring.api.API.models.Likes;
import com.spring.api.API.models.PostViewed;
import com.spring.api.API.models.Posts;
import com.spring.api.API.models.Profiles;
import com.spring.api.API.models.User;
import com.spring.api.API.models.DTOs.Posts.CreatePostDTO;
import com.spring.api.API.models.DTOs.Posts.PostResponse;
import com.spring.api.API.models.DTOs.Posts.UpdatePostDTO;
import com.spring.api.API.security.Exceptions.PostNotFoundException;
import com.spring.api.API.security.Exceptions.UserNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostsService {
    private final IPostsRepository repository;
    private final IProfileRepository profileRepository;
    private final ILikeRepository likeRepository;
    private final IFollowsRepository followsRepository;
    private final IUserRepository userRepository;
    private final IPostViewedRepository postViewedRepository;

    public PostsService(IPostsRepository repository,
        IProfileRepository profileRepository,
        ILikeRepository likeRepository,
        IFollowsRepository followsRepository,
        IUserRepository userRepository,
        IPostViewedRepository postViewedRepository
    ) {
        this.repository = repository;
        this.profileRepository = profileRepository;
        this.likeRepository = likeRepository;
        this.followsRepository = followsRepository;
        this.userRepository = userRepository;
        this.postViewedRepository = postViewedRepository;
    }

    @Transactional
    public PostResponse create(CreatePostDTO dto, String username) {
        Profiles profile = this.profileRepository.findProfilesByUserUsername(username)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        Posts post = this.repository.save(new Posts(
            dto,
            profile
        ));

        return new PostResponse(
            post.getId(),
            post.getDescription(),
            post.getPicture(),
            username,
            0L,
            post.getDatecreated()
        );
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUsername(String username) {
        User curr = this.userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Something went wrong"));

        return this.repository.findMePosts(curr);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> feed(String username) {
        List<Long> userIds = this.followsRepository.findFollowedUserIdsByFollowerUsername(username);
        Pageable pageable = PageRequest.of(0, 20);
        return this.repository.findFeed(userIds, pageable);
    }

    @Transactional
    public String setLike(Long post_id, String username){
        Posts post = this.repository.findById(post_id)
            .orElseThrow(() -> new PostNotFoundException("Post not found"));

        User user = this.userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Something went wrong!"));

        Optional<Likes> like = this.likeRepository.findLikeByUserAndPost(user.getId(), post_id);
        if (like.isEmpty()){
            this.likeRepository.save(new Likes(user, post));
            return "Liked";
        }
        
        if (!post.getProfile().getUser().getId().equals(user.getId())) {
            this.postViewedRepository.save(new PostViewed(post, user));
        }
        
        this.likeRepository.delete(like.get());
        return "Unliked";
    }

    @Transactional
    public PostResponse updatePost(UpdatePostDTO data, Long id){
        Posts post = this.repository.findById(id)
            .orElseThrow(() -> new PostNotFoundException(null));

        post.setDescription(data.getDescription());
        
        this.repository.save(post);
        return new PostResponse(
            post.getId(),
            post.getDescription(),
            post.getPicture(),
            null,
            this.likeRepository.countLikesByPostId(post.getId()),
            post.getDatecreated()
        );
    }

    @Transactional
    public String deletePost(Long post_id, String username){
        User curr = this.userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("Not found"));

        Posts post = this.repository.findPostByUserAndPostId(curr, post_id)
            .orElseThrow(() -> new PostNotFoundException("Something went wrong"));
        
        try{
            this.likeRepository.deleteByPostId(post_id);

            this.repository.delete(post);
            return "Sucessfully!";
        }
        catch(RuntimeException e){
            return "Something went wrong!";
        }
    }

    @Transactional(readOnly = true)
    public PostResponse findPostById(Long post_id){
        return this.repository.findPostResponseById(post_id)
            .orElseThrow(() -> new PostNotFoundException("Not found"));
    }
}
