package com.spring.api.API.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spring.api.API.models.PostViewed;

public interface IPostViewedRepository extends JpaRepository<PostViewed, Long>{
         
}
