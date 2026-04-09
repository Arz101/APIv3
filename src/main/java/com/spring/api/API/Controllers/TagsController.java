package com.spring.api.API.Controllers;

import com.spring.api.API.services.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController("tags")
public class TagsController {

    private final TagService tagService;

    public TagsController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/{name}/related")
    public ResponseEntity<?> getOccurrencesByHashtag(@PathVariable("name") String name,
                                                     @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(tagService.getMostHashOccurrencesByHash(name));
    }

    @GetMapping("/liked")
    public ResponseEntity<?> getTagsLikedByUser(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(this.tagService.tagsLikedByUser(user));
    }
}
