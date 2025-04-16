package com.mocicarazvan.fileservice.testUtils;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import reactor.core.publisher.Flux;


public class ReactiveTestGridFsResource extends ReactiveGridFsResource {

    private final Flux<DataBuffer> downloadStream;

    public ReactiveTestGridFsResource(String filename, Flux<DataBuffer> downloadStream) {
        super(filename, null);
        this.downloadStream = downloadStream;
    }

    public ReactiveTestGridFsResource(Flux<DataBuffer> downloadStream) {
        super("test.txt", null);
        this.downloadStream = downloadStream;
    }

    public ReactiveTestGridFsResource(DataBuffer... dataBuffers) {
        super("test.txt", null);
        this.downloadStream = Flux.fromArray(dataBuffers);
    }

    @Override
    public Flux<DataBuffer> getDownloadStream() {
        return downloadStream;
    }

    @Override
    public Flux<DataBuffer> getDownloadStream(int chunkSize) {
        return downloadStream;
    }
}
