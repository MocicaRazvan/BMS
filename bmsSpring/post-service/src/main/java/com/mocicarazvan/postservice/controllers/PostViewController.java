package com.mocicarazvan.postservice.controllers;


import com.mocicarazvan.postservice.dtos.summaries.PostCountSummaryResponse;
import com.mocicarazvan.postservice.services.PostViewCountService;
import com.mocicarazvan.postservice.services.impl.PostViewCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostViewController {

    private final PostViewCacheService postViewCacheService;
    private final PostViewCountService postViewCountService;

    @PostMapping("/viewCount/{postId}")
    public Mono<ResponseEntity<Void>> incrementViewCount(@PathVariable Long postId) {
        return postViewCacheService.incrementView(postId)
                .map(_ -> ResponseEntity.ok().build());
    }

    @GetMapping(value = "/viewCount/{postId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<Long>> getViewCount(@PathVariable Long postId,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate accessedStart,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate accessedEnd) {
        return postViewCacheService.getCache(postId, accessedStart, accessedEnd)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/admin/viewStats", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<List<PostCountSummaryResponse>>> getPostViewCount(
            @RequestParam(required = false, defaultValue = "3") int top,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate accessedStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate accessedEnd
    ) {
        return postViewCountService.getPostViewCount(top, accessedStart, accessedEnd)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/viewStats/{userId}", produces = {MediaType.APPLICATION_NDJSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Mono<ResponseEntity<List<PostCountSummaryResponse>>> getPostViewCountByUser(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "3") int top,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate accessedStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate accessedEnd
    ) {
        return postViewCountService.getPostViewCount(top, userId, accessedStart, accessedEnd)
                .map(ResponseEntity::ok);
    }
}
