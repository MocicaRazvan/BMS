package com.mocicarazvan.postservice.services;

import com.mocicarazvan.postservice.dtos.PostBody;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.dtos.comments.CommentResponse;
import com.mocicarazvan.postservice.mappers.PostMapper;
import com.mocicarazvan.postservice.models.Post;
import com.mocicarazvan.postservice.repositories.PostRepository;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithChildList;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.services.ApprovedService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface PostService extends ApprovedService<Post, PostBody, PostResponse, PostRepository, PostMapper> {

    Mono<List<String>> seedEmbeddings();

    Mono<Void> existsByIdAndApprovedIsTrue(Long id);

    Mono<ResponseWithChildList<PostResponse, ResponseWithUserDto<CommentResponse>>>
    getPostWithComments(Long id, boolean approved);

    Flux<PageableResponse<ResponseWithUserDto<PostResponse>>> getPostsFilteredWithUser(String title, PageableBody pageableBody, String userId, Boolean approved, List<String> tags, Boolean liked, Boolean admin);

    Flux<PageableResponse<PostResponse>> getPostsFiltered(String title, PageableBody pageableBody, String userId, Boolean approved, List<String> tags, Boolean liked, Boolean admin);

    Flux<PageableResponse<PostResponse>> getModelsTrainer(String title, Long trainerId, PageableBody pageableBody, String userId, Boolean approved, List<String> tags);
}
