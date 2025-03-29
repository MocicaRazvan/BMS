package com.mocicarazvan.templatemodule.services;

import com.mocicarazvan.templatemodule.clients.DownloadFileClient;
import com.mocicarazvan.templatemodule.enums.FileType;
import com.mocicarazvan.templatemodule.utils.SimpleTaskExecutorsInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
public abstract class MockItemService<T> {
    protected final DownloadFileClient downloadFileClient;
    private final TransactionalOperator transactionalOperator;
    private final int parallelism;
    private final Executor executor;

    protected MockItemService(TransactionalOperator transactionalOperator, int parallelism) {
        this.transactionalOperator = transactionalOperator;
        this.parallelism = parallelism;
        this.executor = new SimpleTaskExecutorsInstance().initializeVirtual(4 * parallelism);
        this.downloadFileClient = new DownloadFileClient(parallelism, executor);
    }


    protected abstract Mono<Pair<T, List<FilePart>>> mockItemsBase(Long itemId, String userId);

    protected Mono<List<FilePart>> getFiles(List<String> urls, FileType fileType) {
        return downloadFileClient.downloadImages(urls, fileType)
                .collectList();
    }

    protected String createTitle(String origTitle) {
        return origTitle + "_GENERATED";
    }

    public Flux<T> mockItems(Long itemId, String userId, int n) {
        return Flux.range(1, n)
                .flatMap(_ -> mockItemsBase(itemId, userId)
                                .flatMap(pair ->
                                        Flux.fromIterable(pair.getSecond())
                                                .flatMap(
                                                        part -> Mono.defer(part::delete)
                                                                .subscribeOn(Schedulers.fromExecutor(executor))
                                                        , parallelism)
                                                .then(Mono.just(pair.getFirst()))

                                )
                        , parallelism)
                .subscribeOn(Schedulers.fromExecutor(executor))
                .as(transactionalOperator::transactional);

    }

}
