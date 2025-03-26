package com.mocicarazvan.postservice.services;

import com.mocicarazvan.postservice.dtos.summaries.PostCountSummaryResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface PostViewCountService {
    Mono<List<PostCountSummaryResponse>> getPostViewCount(int top, Long userId, LocalDate accessedStart, LocalDate accessedEnd);

    Mono<List<PostCountSummaryResponse>> getPostViewCount(int top, LocalDate accessedStart, LocalDate accessedEnd);
}
