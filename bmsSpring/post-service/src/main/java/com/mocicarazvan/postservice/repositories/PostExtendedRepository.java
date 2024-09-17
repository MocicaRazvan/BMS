package com.mocicarazvan.postservice.repositories;


import com.mocicarazvan.postservice.models.Post;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface PostExtendedRepository {

    Flux<Post> getPostsFiltered(String title, Boolean approved, List<String> tags, Long likedUserId, PageRequest pageRequest);

    Flux<Post> getPostsFilteredTrainer(String title, Boolean approved, List<String> tags, Long trainerId, PageRequest pageRequest);

    Mono<Long> countPostsFiltered(String title, Boolean approved, List<String> tags, Long likedUserId);

    Mono<Long> countPostsFilteredTrainer(String title, Boolean approved, Long trainerId, List<String> tags);

}
