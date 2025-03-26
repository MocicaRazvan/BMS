package com.mocicarazvan.postservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mocicarazvan.postservice.dtos.summaries.PostCountSummaryResponse;
import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.repositories.PostViewCountRepository;
import com.mocicarazvan.postservice.services.PostViewCountService;
import com.mocicarazvan.rediscache.config.FlushProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostViewCountServiceImpl implements PostViewCountService {
    private final PostViewCountRepository postViewCountRepository;
    private final PostMapper postMapper;
    private static final String BASE_KEY = "postViewCnt";
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final FlushProperties flushProperties;


    @Override
    @SuppressWarnings("unchecked")
    public Mono<List<PostCountSummaryResponse>> getPostViewCount(int top, Long userId, LocalDate accessedStart, LocalDate accessedEnd) {
        String key = createKey(top, userId);
        return
                redisTemplate.opsForValue().get(key)
                        .map(
                                collection -> (List<PostCountSummaryResponse>)
                                        objectMapper.convertValue(
                                                collection, objectMapper.getTypeFactory()
                                                        .constructCollectionType(
                                                                List.class, PostCountSummaryResponse.class
                                                        )
                                        )
                        )
                        .switchIfEmpty(Mono.defer(() -> (
                                postViewCountRepository.findTopPostsByCount(top, userId, accessedStart, accessedEnd)
                                        .map(p -> PostCountSummaryResponse.fromResponse(
                                                postMapper.fromModelToResponse(p),
                                                p.getViewCount(), p.getRank()
                                        ))
//                                        .log()
                                        .collectSortedList(Comparator.comparingInt(PostCountSummaryResponse::getRank))
                                        .flatMap(
                                                list -> redisTemplate.opsForValue().set(key, list, Duration.ofSeconds(flushProperties.getTimeout()))
                                                        .thenReturn(list)
                                        )
                        )));
    }

    @Override
    public Mono<List<PostCountSummaryResponse>> getPostViewCount(int top, LocalDate accessedStart, LocalDate accessedEnd) {
        return getPostViewCount(top, null, accessedStart, accessedEnd);
    }

    public String createKey(int top, Long userId) {
        return BASE_KEY + ":top:" + top + ":userId:" + userId;
    }

}
