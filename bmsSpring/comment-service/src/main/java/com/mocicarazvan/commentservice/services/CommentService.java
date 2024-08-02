package com.mocicarazvan.commentservice.services;


import com.mocicarazvan.commentservice.dtos.CommentBody;
import com.mocicarazvan.commentservice.dtos.CommentResponse;
import com.mocicarazvan.commentservice.enums.CommentReferenceType;
import com.mocicarazvan.commentservice.mappers.CommentMapper;
import com.mocicarazvan.commentservice.models.Comment;
import com.mocicarazvan.commentservice.repositories.CommentRepository;
import com.mocicarazvan.templatemodule.dtos.PageableBody;
import com.mocicarazvan.templatemodule.dtos.response.PageableResponse;
import com.mocicarazvan.templatemodule.dtos.response.ResponseWithUserDto;
import com.mocicarazvan.templatemodule.enums.Role;
import com.mocicarazvan.templatemodule.services.TitleBodyService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentService extends TitleBodyService<Comment, CommentBody, CommentResponse, CommentRepository, CommentMapper> {

    Mono<CommentResponse> createModel(Long postId, CommentBody commentBody, String userId, CommentReferenceType referenceType);


    Flux<PageableResponse<CommentResponse>> getCommentsByReference(Long postId, PageableBody pageableBody, CommentReferenceType commentReferenceType);


    Flux<PageableResponse<ResponseWithUserDto<CommentResponse>>> getCommentsWithUserByReference(Long postId, PageableBody pageableBody, CommentReferenceType referenceType);


    Flux<ResponseWithUserDto<CommentResponse>> getCommentsByReference(Long postId, CommentReferenceType referenceType);

    Flux<PageableResponse<CommentResponse>> getModelByUser(Long userId, PageableBody pageableBody, CommentReferenceType referenceType);


    Mono<Void> deleteCommentsByReference(Long postId, String userId, Role role, CommentReferenceType referenceType);
}
