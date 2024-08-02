package com.mocicarazvan.postservice.repositories;


import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.templatemodule.utils.PageableUtilsCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface PostExtendedRepository {

    Flux<Post> getPostsFiltered(String title, Boolean approved, List<String> tags, PageRequest pageRequest);

    Flux<Post> getPostsFilteredTrainer(String title, Boolean approved, List<String> tags, Long trainerId, PageRequest pageRequest);

    Mono<Long> countPostsFiltered(String title, Boolean approved, List<String> tags);

    Mono<Long> countPostsFilteredTrainer(String title, Boolean approved, Long trainerId, List<String> tags);

}
