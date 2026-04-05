package com.spring.api.API.models.DTOs.User;

import com.spring.api.API.models.DTOs.Posts.PostResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRanking {
    List<PostResponse> feed;
}
