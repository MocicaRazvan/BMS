package com.mocicarazvan.postservice.services.impl;


import com.mocicarazvan.postservice.dtos.PostBody;
import com.mocicarazvan.postservice.dtos.PostResponse;
import com.mocicarazvan.postservice.services.PostService;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.services.MockItemService;
import com.mocicarazvan.templatemodule.utils.Randomizer;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class MockPostService extends MockItemService<PostResponse> {
    private final PostService postService;
    private static final List<String> tags =
            List.of(
                    "#wellness",
                    "#fitness",
                    "#nutrition",
                    "#mentalhealth",
                    "#yoga",
                    "#meditation",
                    "#mindfulness",
                    "#selfcare"
            );


    public MockPostService(PostService postService, TransactionalOperator transactionalOperator) {
        super(transactionalOperator, 10);
        this.postService = postService;
    }

    @Override
    protected Mono<Pair<PostResponse, List<FilePart>>> mockItemsBase(Long itemId, String userId) {
        return postService.getModel(itemId)
                .flatMap(post ->
                        {
                            PostBody postBody = PostBody.builder()
                                    .tags(Randomizer.pickRandomItemsFromList(tags))
                                    .body(post.getBody())
                                    .title(createTitle(post.getTitle()))
                                    .build();
                            return getFiles(post.getImages(), FileType.IMAGE)
                                    .flatMap(images ->
                                            postService.createModel(
                                                            Flux.fromIterable(Randomizer.shuffleList(images)),
                                                            postBody,
                                                            userId,
                                                            UUID.randomUUID().toString())
                                                    .map(postResponse -> Pair.of(postResponse, images))
                                    );
                        }
                );
    }
}
